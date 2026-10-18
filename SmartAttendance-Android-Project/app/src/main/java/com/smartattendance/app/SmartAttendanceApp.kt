package com.smartattendance.app

import android.app.Application
import com.smartattendance.app.data.local.AppDatabase
import com.smartattendance.app.data.repository.AttendanceRepository

class SmartAttendanceApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: AttendanceRepository by lazy {
        AttendanceRepository(database.studentDao(), database.attendanceDao())
    }
}
