package com.elnaz.brushbuddy.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
//Defines the stored session data
//An Entity is a Kotlin data class
// that represents a table in the Room database.
@Entity(tableName = "brushing_sessions")
data class BrushingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val durationSeconds: Int
)