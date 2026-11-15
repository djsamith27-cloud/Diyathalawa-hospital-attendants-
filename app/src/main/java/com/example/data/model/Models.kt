package com.example.data.model

import kotlinx.serialization.Serializable

enum class ShiftMode(val value: String) {
    DAY("day"),
    NIGHT("night");

    companion object {
        fun fromValue(value: String?): ShiftMode =
            if (value == NIGHT.value) NIGHT else DAY
    }
}

enum class ShiftType(val value: String) {
    BOTH("both"),
    DAY("day"),
    NIGHT("night");

    companion object {
        fun fromValue(value: String?): ShiftType = when (value) {
            DAY.value -> DAY
            NIGHT.value -> NIGHT
            else -> BOTH
        }
    }
}

enum class AttendanceStatus(val value: String) {
    PRESENT("present"),
    ABSENT("absent"),
    LEAVE("leave");

    companion object {
        fun fromValue(value: String?): AttendanceStatus = when (value) {
            ABSENT.value -> ABSENT
            LEAVE.value -> LEAVE
            else -> PRESENT
        }
    }
}

@Serializable
data class BackupEmployee(
    val id: String,
    val name: String,
    val empNo: String,
    val role: String = "",
    val shiftType: String = "both",
    val joinDate: String = "",
    val nic: String = "",
    val phone: String = "",
    val emergency: String = "",
    val blood: String = "",
    val dob: String = "",
    val address: String = ""
)

@Serializable
data class BackupDayNightStatus(
    val day: String = "present",
    val night: String = "present"
)

@Serializable
data class BackupPayload(
    val employees: List<BackupEmployee> = emptyList(),
    val attendance: Map<String, Map<String, BackupDayNightStatus>> = emptyMap(),
    val lockedDates: Map<String, Boolean> = emptyMap()
)

object EmployeeUtils {
    private val digitsRegex = Regex("\\D")
    private val phoneRegex = Regex("0\\d{9}")

    fun extractEmpNumber(empNo: String): Int {
        val digits = empNo.replace(digitsRegex, "")
        return digits.toIntOrNull() ?: 0
    }

    fun compareEmpNo(a: String, b: String): Int {
        val numA = extractEmpNumber(a)
        val numB = extractEmpNumber(b)
        if (numA != numB) return numA.compareTo(numB)
        return a.compareTo(b)
    }

    fun computeNextEmpNo(existingEmpNos: List<String>): String {
        if (existingEmpNos.isEmpty()) return "EMP001"
        val sorted = existingEmpNos.sortedWith(::compareEmpNo)
        val lastEmpNo = sorted.last()
        val extracted = extractEmpNumber(lastEmpNo).takeIf { it > 0 } ?: existingEmpNos.size
        val nextNum = extracted + 1
        return "EMP" + nextNum.toString().padStart(3, '0')
    }

    fun extractDialPhone(raw: String): String {
        val match = phoneRegex.find(raw)
        return match?.value ?: raw.replace(digitsRegex, "")
    }
}
