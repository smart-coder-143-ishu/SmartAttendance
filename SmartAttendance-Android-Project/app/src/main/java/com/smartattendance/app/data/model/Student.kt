package com.smartattendance.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room SQLite Entity representing a Student record.
 * Unique index on rollNumber and className prevents duplicate rolls in the same class.
 */
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["rollNumber", "className"], unique = true)
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val className: String,
    val createdAt: Long = System.currentTimeMillis()
)
