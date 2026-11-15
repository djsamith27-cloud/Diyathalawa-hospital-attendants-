package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AttendanceEntity
import com.example.data.local.EmployeeEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmployeeUtils
import com.example.data.model.ShiftMode
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AttendanceRowUi(
    val employee: EmployeeEntity,
    val status: AttendanceStatus
)

data class AttendanceUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val currentShift: ShiftMode = ShiftMode.DAY,
    val isLocked: Boolean = false,
    val rows: List<AttendanceRowUi> = emptyList(),
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0
)

enum class ReportCellState {
    BEFORE_JOIN,
    UNRECORDED,
    PRESENT,
    ABSENT,
    LEAVE
}

data class ReportEmployeeRow(
    val employee: EmployeeEntity,
    val dayCells: List<ReportCellState>
)

data class MonthlyReportUiState(
    val monthStr: String = YearMonth.now().toString(),
    val daysInMonth: Int = YearMonth.now().lengthOfMonth(),
    val dayShiftRows: List<ReportEmployeeRow> = emptyList(),
    val nightShiftRows: List<ReportEmployeeRow> = emptyList()
)

class AttendanceViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentShift = MutableStateFlow(ShiftMode.DAY)
    val currentShift: StateFlow<ShiftMode> = _currentShift.asStateFlow()

    private val _reportMonth = MutableStateFlow(YearMonth.now().toString())
    val reportMonth: StateFlow<String> = _reportMonth.asStateFlow()

    private val _employeeSearchQuery = MutableStateFlow("")
    val employeeSearchQuery: StateFlow<String> = _employeeSearchQuery.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessages: SharedFlow<String> = _snackbarMessages.asSharedFlow()

    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allEmployees: StateFlow<List<EmployeeEntity>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredEmployees: StateFlow<List<EmployeeEntity>> = combine(
        allEmployees,
        _employeeSearchQuery
    ) { employees, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            employees
        } else {
            employees.filter { emp ->
                emp.name.lowercase().contains(q) ||
                    emp.empNo.lowercase().contains(q) ||
                    emp.nic.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceUiState: StateFlow<AttendanceUiState> = combine(
        _selectedDate,
        _currentShift,
        allEmployees,
        repository.allAttendance,
        repository.lockedDates
    ) { date, shift, employees, allAtt, lockedMap ->
        val isLocked = lockedMap[date] == true
        val attForDate = allAtt.filter { it.date == date }.associateBy { it.employeeId }

        val eligible = employees.filter { emp ->
            val sType = emp.shiftType.ifBlank { "both" }
            val matchesShift = sType == "both" || sType == shift.value
            val matchesJoin = emp.joinDate.isBlank() || date >= emp.joinDate
            matchesShift && matchesJoin
        }

        var present = 0
        var absent = 0
        var leave = 0

        val rows = eligible.map { emp ->
            val rec = attForDate[emp.id]
            val rawStatus = when (shift) {
                ShiftMode.DAY -> rec?.dayStatus
                ShiftMode.NIGHT -> rec?.nightStatus
            }
            val status = AttendanceStatus.fromValue(rawStatus)
            when (status) {
                AttendanceStatus.PRESENT -> present++
                AttendanceStatus.ABSENT -> absent++
                AttendanceStatus.LEAVE -> leave++
            }
            AttendanceRowUi(employee = emp, status = status)
        }

        AttendanceUiState(
            selectedDate = date,
            currentShift = shift,
            isLocked = isLocked,
            rows = rows,
            presentCount = present,
            absentCount = absent,
            leaveCount = leave
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AttendanceUiState())

    val monthlyReportState: StateFlow<MonthlyReportUiState> = combine(
        _reportMonth,
        allEmployees,
        repository.allAttendance
    ) { monthStr, employees, allAtt ->
        buildMonthlyReport(monthStr, employees, allAtt)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlyReportUiState())

    private val clockFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy | hh:mm:ss a", Locale.US)

    val liveDateTimeText: StateFlow<String> = flow {
        while (true) {
            emit(LocalDateTime.now().format(clockFormatter))
            delay(1000L)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LocalDateTime.now().format(clockFormatter)
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialSeed()
            repository.ensureAttendanceInitializedForDate(_selectedDate.value)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            repository.setDarkTheme(!isDarkTheme.value)
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.ensureAttendanceInitializedForDate(date)
        }
    }

    fun shiftDateByDays(deltaDays: Long) {
        val current = runCatching { LocalDate.parse(_selectedDate.value) }.getOrNull() ?: LocalDate.now()
        selectDate(current.plusDays(deltaDays).toString())
    }

    fun setShiftMode(shift: ShiftMode) {
        _currentShift.value = shift
        viewModelScope.launch {
            repository.ensureAttendanceInitializedForDate(_selectedDate.value)
        }
    }

    fun setEmployeeStatus(employeeId: String, status: AttendanceStatus) {
        val date = _selectedDate.value
        val shift = _currentShift.value
        viewModelScope.launch {
            repository.setEmployeeStatus(date, employeeId, shift, status)
        }
    }

    fun markAllCurrentShift(status: AttendanceStatus) {
        val date = _selectedDate.value
        val shift = _currentShift.value
        viewModelScope.launch {
            repository.markAllForShift(date, shift, status)
        }
    }

    fun setDateLocked(locked: Boolean) {
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.setDateLocked(date, locked)
        }
    }

    fun updateSearchQuery(query: String) {
        _employeeSearchQuery.value = query
    }

    fun computeSuggestedEmpNo(): String {
        return EmployeeUtils.computeNextEmpNo(allEmployees.value.map { it.empNo })
    }

    fun saveEmployee(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.saveEmployee(employee)
            repository.ensureAttendanceInitializedForDate(_selectedDate.value)
            _snackbarMessages.tryEmit("සේවක තොරතුරු සාර්ථකව සුරකින ලදී.")
        }
    }

    fun deleteEmployee(id: String) {
        viewModelScope.launch {
            repository.deleteEmployee(id)
            _snackbarMessages.tryEmit("සේවකයා ඉවත් කරන ලදී.")
        }
    }

    fun selectReportMonth(monthStr: String) {
        _reportMonth.value = monthStr
    }

    fun shiftReportMonth(deltaMonths: Long) {
        val current = runCatching { YearMonth.parse(_reportMonth.value) }.getOrNull() ?: YearMonth.now()
        _reportMonth.value = current.plusMonths(deltaMonths).toString()
    }

    suspend fun getBackupJson(): String = repository.exportBackupJson()

    suspend fun getMonthlyCsv(shift: ShiftMode): String =
        repository.buildMonthlyReportCsv(_reportMonth.value, shift)

    suspend fun getEmployeesCsv(): String = repository.buildEmployeesCsv()

    fun restoreFromJson(rawJson: String) {
        viewModelScope.launch {
            val result = repository.importBackupJson(rawJson)
            result.onSuccess { count ->
                _snackbarMessages.tryEmit("දත්ත සාර්ථකව ප්‍රතිස්ථාපනය (Restore) විය! (සේවකයන් $count)")
            }.onFailure {
                _snackbarMessages.tryEmit("වලංගු නොවන Backup ගොනුවකි හෝ කියවීමේ දෝෂයකි.")
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            _snackbarMessages.tryEmit("සියලු දත්ත ඉවත් කරන ලදී.")
        }
    }

    fun notifyMessage(msg: String) {
        _snackbarMessages.tryEmit(msg)
    }

    private fun buildMonthlyReport(
        monthStr: String,
        employees: List<EmployeeEntity>,
        allAttendance: List<AttendanceEntity>
    ): MonthlyReportUiState {
        val ym = runCatching { YearMonth.parse(monthStr) }.getOrNull() ?: YearMonth.now()
        val daysInMonth = ym.lengthOfMonth()
        val attLookup = allAttendance.associateBy { "${it.date}|${it.employeeId}" }

        val dayEmployees = employees.filter {
            val sType = it.shiftType.ifBlank { "both" }
            sType == "both" || sType == "day"
        }
        val nightEmployees = employees.filter {
            val sType = it.shiftType.ifBlank { "both" }
            sType == "both" || sType == "night"
        }

        fun buildRows(targetEmployees: List<EmployeeEntity>, shift: ShiftMode): List<ReportEmployeeRow> {
            return targetEmployees.map { emp ->
                val cells = (1..daysInMonth).map { day ->
                    val dateKey = "%04d-%02d-%02d".format(ym.year, ym.monthValue, day)
                    if (emp.joinDate.isNotBlank() && dateKey < emp.joinDate) {
                        ReportCellState.BEFORE_JOIN
                    } else {
                        val rec = attLookup["$dateKey|${emp.id}"]
                        if (rec == null) {
                            ReportCellState.UNRECORDED
                        } else {
                            val st = when (shift) {
                                ShiftMode.DAY -> rec.dayStatus
                                ShiftMode.NIGHT -> rec.nightStatus
                            }
                            when (st) {
                                "present" -> ReportCellState.PRESENT
                                "absent" -> ReportCellState.ABSENT
                                "leave" -> ReportCellState.LEAVE
                                else -> ReportCellState.UNRECORDED
                            }
                        }
                    }
                }
                ReportEmployeeRow(employee = emp, dayCells = cells)
            }
        }

        return MonthlyReportUiState(
            monthStr = ym.toString(),
            daysInMonth = daysInMonth,
            dayShiftRows = buildRows(dayEmployees, ShiftMode.DAY),
            nightShiftRows = buildRows(nightEmployees, ShiftMode.NIGHT)
        )
    }

    class Factory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AttendanceViewModel(repository) as T
        }
    }
}
