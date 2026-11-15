package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.ThemePreferences
import com.example.data.repository.AttendanceRepository

class AttendanceApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferences: ThemePreferences by lazy { ThemePreferences(this) }
    val repository: AttendanceRepository by lazy {
        AttendanceRepository(database, preferences)
    }
}
