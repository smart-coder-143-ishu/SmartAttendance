package com.smartattendance.app.data.local

import androidx.room.*
import com.smartattendance.app.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT DISTINCT date FROM attendance_records ORDER BY date DESC")
    fun getAllRecordedDates(): Flow<List<String>>

    /**
     * Upsert single record. If record with same studentId and date exists, replaces it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE date = :date")
    suspend fun deleteAttendanceForDate(date: String)

    @Query("SELECT COUNT(DISTINCT date) FROM attendance_records")
    fun getTotalRecordedDays(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND isPresent = 1")
    fun getPresentCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND isPresent = 0")
    fun getAbsentCountForDate(date: String): Flow<Int>
}
