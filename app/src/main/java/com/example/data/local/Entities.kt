package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "attendance_records",
    primaryKeys = ["date", "employeeId"]
)
data class AttendanceEntity(
    val date: String,
    val employeeId: String,
    val dayStatus: String = "present",
    val nightStatus: String = "present"
)

@Entity(tableName = "locked_dates")
data class LockedDateEntity(
    @PrimaryKey val date: String,
    val isLocked: Boolean = false
)
