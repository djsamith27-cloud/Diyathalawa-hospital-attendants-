package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesOnce(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEmployees(employees: List<EmployeeEntity>)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: String)

    @Query("DELETE FROM employees")
    suspend fun deleteAllEmployees()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllAttendanceOnce(): List<AttendanceEntity>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceForDateOnce(date: String): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(record: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AttendanceEntity>)

    @Query("DELETE FROM attendance_records WHERE employeeId = :employeeId")
    suspend fun deleteAttendanceForEmployee(employeeId: String)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllAttendance()
}

@Dao
interface LockedDateDao {
    @Query("SELECT * FROM locked_dates")
    fun getAllLockedDates(): Flow<List<LockedDateEntity>>

    @Query("SELECT * FROM locked_dates")
    suspend fun getAllLockedDatesOnce(): List<LockedDateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLockedDate(lockedDate: LockedDateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lockedDates: List<LockedDateEntity>)

    @Query("DELETE FROM locked_dates")
    suspend fun deleteAllLockedDates()
}
