package com.smartattendance.app.data.local

import androidx.room.*
import com.smartattendance.app.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY className ASC, CAST(rollNumber AS INTEGER) ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Query("SELECT DISTINCT className FROM students ORDER BY className ASC")
    fun getAllClasses(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}
