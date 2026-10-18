package com.smartattendance.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room SQLite Entity representing an attendance mark for a student on a specific date.
 * 
 * Key Constraints:
 * 1. Unique index on [studentId, date] strictly enforces:
 *    "Prevent duplicate attendance records for the same student on the same date."
 * 2. ForeignKey with CASCADE ensures that if a student is deleted, their attendance records are purged.
 */
@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // ISO Format: YYYY-MM-DD
    val isPresent: Boolean,
    val remarks: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
