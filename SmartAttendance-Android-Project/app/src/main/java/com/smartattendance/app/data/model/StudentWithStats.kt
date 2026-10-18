package com.smartattendance.app.data.model

data class StudentWithStats(
    val student: Student,
    val totalRecordedDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val attendancePercentage: Float // Formula: (presentDays / totalRecordedDays) * 100
) {
    val isLowAttendance: Boolean
        get() = totalRecordedDays > 0 && attendancePercentage < 75.0f
}
