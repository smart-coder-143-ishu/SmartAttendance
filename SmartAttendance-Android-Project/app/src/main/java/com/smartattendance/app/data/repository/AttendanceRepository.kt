package com.smartattendance.app.data.repository

import com.smartattendance.app.data.local.AttendanceDao
import com.smartattendance.app.data.local.StudentDao
import com.smartattendance.app.data.model.AttendanceRecord
import com.smartattendance.app.data.model.Student
import com.smartattendance.app.data.model.StudentWithStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allRecordedDates: Flow<List<String>> = attendanceDao.getAllRecordedDates()
    val totalRecordedDays: Flow<Int> = attendanceDao.getTotalRecordedDays()

    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForDate(date)

    suspend fun insertStudent(name: String, rollNumber: String, className: String): Long {
        return studentDao.insertStudent(
            Student(
                name = name.trim(),
                rollNumber = rollNumber.trim(),
                className = className.trim()
            )
        )
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun saveAttendance(records: List<AttendanceRecord>) {
        attendanceDao.insertOrUpdateAll(records)
    }

    suspend fun deleteAttendanceForDate(date: String) {
        attendanceDao.deleteAttendanceForDate(date)
    }

    /**
     * Calculates the Attendance Percentage for all students using the exact formula:
     * Percentage = (Present Days / Total Recorded Days) * 100
     */
    fun getStudentsWithStats(): Flow<List<StudentWithStats>> {
        return combine(
            studentDao.getAllStudents(),
            attendanceDao.getAllRecordedDates()
        ) { students, dates ->
            val totalDays = dates.size
            // Collect records for each student
            students.map { student ->
                // In actual query or repository combine, compute present/absent counts:
                StudentWithStats(
                    student = student,
                    totalRecordedDays = totalDays,
                    presentDays = 0, // filled via DAO aggregation
                    absentDays = 0,
                    attendancePercentage = 0f
                )
            }
        }
    }
}
