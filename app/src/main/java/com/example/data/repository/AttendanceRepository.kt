package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.LockedDateEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.BackupDayNightStatus
import com.example.data.model.BackupEmployee
import com.example.data.model.BackupPayload
import com.example.data.model.EmployeeUtils
import com.example.data.model.ShiftMode
import com.example.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.YearMonth

class AttendanceRepository(
    private val database: AppDatabase,
    private val preferences: ThemePreferences
) {
    private val employeeDao = database.employeeDao()
    private val attendanceDao = database.attendanceDao()
    private val lockedDateDao = database.lockedDateDao()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    val employees: Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees().map { list ->
        list.sortedWith { a, b -> EmployeeUtils.compareEmpNo(a.empNo, b.empNo) }
    }

    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendance()

    val lockedDates: Flow<Map<String, Boolean>> = lockedDateDao.getAllLockedDates().map { list ->
        list.associate { it.date to it.isLocked }
    }

    val isDarkTheme: Flow<Boolean> = preferences.isDarkTheme

    suspend fun setDarkTheme(isDark: Boolean) {
        preferences.setDarkTheme(isDark)
    }

    suspend fun ensureInitialSeed() {
        if (!preferences.isInitialSeedDone()) {
            val existing = employeeDao.getAllEmployeesOnce()
            if (existing.isEmpty()) {
                employeeDao.upsertEmployee(
                    EmployeeEntity(
                        id = "1",
                        name = "සමිත් මධුසංක",
                        empNo = "EMP001",
                        role = "වැඩ පරීක්ෂක",
                        shiftType = "both",
                        joinDate = "2026-11-01",
                        nic = "921920000V",
                        phone = "0712345678",
                        emergency = "භාර්යාව - 0719876543",
                        blood = "O+",
                        dob = "1992-07-10",
                        address = "දියතලාව"
                    )
                )
            }
            preferences.markInitialSeedDone()
        }
    }

    suspend fun ensureAttendanceInitializedForDate(date: String) {
        val allEmployees = employeeDao.getAllEmployeesOnce()
        if (allEmployees.isEmpty()) return

        val existingForDate = attendanceDao.getAttendanceForDateOnce(date).associateBy { it.employeeId }
        val toInsert = mutableListOf<AttendanceEntity>()

        for (emp in allEmployees) {
            if (emp.joinDate.isNotBlank() && date < emp.joinDate) continue
            if (!existingForDate.containsKey(emp.id)) {
                toInsert.add(
                    AttendanceEntity(
                        date = date,
                        employeeId = emp.id,
                        dayStatus = AttendanceStatus.PRESENT.value,
                        nightStatus = AttendanceStatus.PRESENT.value
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            attendanceDao.upsertAll(toInsert)
        }
    }

    suspend fun setEmployeeStatus(
        date: String,
        employeeId: String,
        shift: ShiftMode,
        status: AttendanceStatus
    ) {
        val locked = lockedDateDao.getAllLockedDatesOnce().firstOrNull { it.date == date }?.isLocked == true
        if (locked) return

        val existing = attendanceDao.getAttendanceForDateOnce(date)
            .firstOrNull { it.employeeId == employeeId }
            ?: AttendanceEntity(date = date, employeeId = employeeId)

        val updated = when (shift) {
            ShiftMode.DAY -> existing.copy(dayStatus = status.value)
            ShiftMode.NIGHT -> existing.copy(nightStatus = status.value)
        }
        attendanceDao.upsertAttendance(updated)
    }

    suspend fun markAllForShift(
        date: String,
        shift: ShiftMode,
        status: AttendanceStatus
    ) {
        val locked = lockedDateDao.getAllLockedDatesOnce().firstOrNull { it.date == date }?.isLocked == true
        if (locked) return

        val allEmployees = employeeDao.getAllEmployeesOnce()
        val existingByEmpId = attendanceDao.getAttendanceForDateOnce(date).associateBy { it.employeeId }

        val updatedRecords = allEmployees.filter { emp ->
            val sType = emp.shiftType.ifBlank { "both" }
            val matchesShift = sType == "both" || sType == shift.value
            val matchesJoin = emp.joinDate.isBlank() || date >= emp.joinDate
            matchesShift && matchesJoin
        }.map { emp ->
            val current = existingByEmpId[emp.id] ?: AttendanceEntity(date = date, employeeId = emp.id)
            when (shift) {
                ShiftMode.DAY -> current.copy(dayStatus = status.value)
                ShiftMode.NIGHT -> current.copy(nightStatus = status.value)
            }
        }
        if (updatedRecords.isNotEmpty()) {
            attendanceDao.upsertAll(updatedRecords)
        }
    }

    suspend fun setDateLocked(date: String, locked: Boolean) {
        lockedDateDao.upsertLockedDate(LockedDateEntity(date = date, isLocked = locked))
    }

    suspend fun saveEmployee(employee: EmployeeEntity) {
        employeeDao.upsertEmployee(employee)
    }

    suspend fun deleteEmployee(id: String) {
        database.withTransaction {
            attendanceDao.deleteAttendanceForEmployee(id)
            employeeDao.deleteEmployeeById(id)
        }
    }

    suspend fun resetAllData() {
        database.withTransaction {
            attendanceDao.deleteAllAttendance()
            lockedDateDao.deleteAllLockedDates()
            employeeDao.deleteAllEmployees()
        }
    }

    suspend fun exportBackupJson(): String {
        val emps = employeeDao.getAllEmployeesOnce()
            .sortedWith { a, b -> EmployeeUtils.compareEmpNo(a.empNo, b.empNo) }
            .map {
                BackupEmployee(
                    id = it.id,
                    name = it.name,
                    empNo = it.empNo,
                    role = it.role,
                    shiftType = it.shiftType,
                    joinDate = it.joinDate,
                    nic = it.nic,
                    phone = it.phone,
                    emergency = it.emergency,
                    blood = it.blood,
                    dob = it.dob,
                    address = it.address
                )
            }

        val attList = attendanceDao.getAllAttendanceOnce()
        val attMap = linkedMapOf<String, MutableMap<String, BackupDayNightStatus>>()
        for (rec in attList) {
            val dayMap = attMap.getOrPut(rec.date) { linkedMapOf() }
            dayMap[rec.employeeId] = BackupDayNightStatus(
                day = rec.dayStatus,
                night = rec.nightStatus
            )
        }

        val locks = lockedDateDao.getAllLockedDatesOnce()
            .filter { it.isLocked }
            .associate { it.date to true }

        val payload = BackupPayload(
            employees = emps,
            attendance = attMap,
            lockedDates = locks
        )
        return json.encodeToString(BackupPayload.serializer(), payload)
    }

    suspend fun importBackupJson(rawJson: String): Result<Int> {
        return runCatching {
            val parsed = json.decodeFromString(BackupPayload.serializer(), rawJson)
            val empEntities = parsed.employees.map {
                EmployeeEntity(
                    id = it.id,
                    name = it.name,
                    empNo = it.empNo,
                    role = it.role,
                    shiftType = it.shiftType.ifBlank { "both" },
                    joinDate = it.joinDate,
                    nic = it.nic,
                    phone = it.phone,
                    emergency = it.emergency,
                    blood = it.blood,
                    dob = it.dob,
                    address = it.address
                )
            }

            val attEntities = mutableListOf<AttendanceEntity>()
            parsed.attendance.forEach { (date, empMap) ->
                empMap.forEach { (empId, status) ->
                    attEntities.add(
                        AttendanceEntity(
                            date = date,
                            employeeId = empId,
                            dayStatus = status.day.ifBlank { "present" },
                            nightStatus = status.night.ifBlank { "present" }
                        )
                    )
                }
            }

            val lockEntities = parsed.lockedDates.map { (date, locked) ->
                LockedDateEntity(date = date, isLocked = locked)
            }

            database.withTransaction {
                attendanceDao.deleteAllAttendance()
                lockedDateDao.deleteAllLockedDates()
                employeeDao.deleteAllEmployees()

                if (empEntities.isNotEmpty()) employeeDao.upsertEmployees(empEntities)
                if (attEntities.isNotEmpty()) attendanceDao.upsertAll(attEntities)
                if (lockEntities.isNotEmpty()) lockedDateDao.upsertAll(lockEntities)
            }

            empEntities.size
        }
    }

    suspend fun buildMonthlyReportCsv(monthStr: String, shiftMode: ShiftMode): String {
        val ym = YearMonth.parse(monthStr)
        val daysInMonth = ym.lengthOfMonth()
        val emps = employeeDao.getAllEmployeesOnce()
            .sortedWith { a, b -> EmployeeUtils.compareEmpNo(a.empNo, b.empNo) }
            .filter { emp ->
                val sType = emp.shiftType.ifBlank { "both" }
                sType == "both" || sType == shiftMode.value
            }

        val attByDateAndEmp = attendanceDao.getAllAttendanceOnce()
            .associateBy { "${it.date}|${it.employeeId}" }

        val sb = StringBuilder()
        sb.append("\uFEFFසේවක අංකය,සේවකයා")
        for (d in 1..daysInMonth) {
            sb.append(",දින $d")
        }
        sb.append("\n")

        for (emp in emps) {
            sb.append("\"${escapeCsv(emp.empNo)}\",\"${escapeCsv(emp.name)}\"")
            for (day in 1..daysInMonth) {
                val dateKey = "%04d-%02d-%02d".format(ym.year, ym.monthValue, day)
                val valStr = if (emp.joinDate.isNotBlank() && dateKey < emp.joinDate) {
                    ""
                } else {
                    val rec = attByDateAndEmp["$dateKey|${emp.id}"]
                    val status = when (shiftMode) {
                        ShiftMode.DAY -> rec?.dayStatus
                        ShiftMode.NIGHT -> rec?.nightStatus
                    }
                    when (status) {
                        "present" -> "Present"
                        "absent" -> "Absent"
                        "leave" -> "Leave"
                        else -> ""
                    }
                }
                sb.append(",\"$valStr\"")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    suspend fun buildEmployeesCsv(): String {
        val emps = employeeDao.getAllEmployeesOnce()
            .sortedWith { a, b -> EmployeeUtils.compareEmpNo(a.empNo, b.empNo) }

        val sb = StringBuilder()
        sb.append("\uFEFFසේවක අංකය,නම,තනතුර,මුරය,NIC,දුරකථන,ලිපිනය\n")
        for (e in emps) {
            val sType = when (e.shiftType) {
                "day" -> "දිවා"
                "night" -> "රාත්‍රි"
                else -> "දිවා/රාත්‍රි"
            }
            sb.append(
                "\"${escapeCsv(e.empNo)}\",\"${escapeCsv(e.name)}\",\"${escapeCsv(e.role)}\",\"$sType\",\"${escapeCsv(e.nic)}\",\"${escapeCsv(e.phone)}\",\"${escapeCsv(e.address)}\"\n"
            )
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String = value.replace("\"", "\"\"")
}
