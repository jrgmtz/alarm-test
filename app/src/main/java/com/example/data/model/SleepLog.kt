package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long, // Epoch timestamp when user fell asleep
    val endTime: Long,   // Epoch timestamp when user woke up
    val rating: Int,     // 1 to 5 stars
    val notes: String = "",
    val factorAlcohol: Boolean = false,
    val factorCaffeine: Boolean = false,
    val factorExercise: Boolean = false,
    val factorStress: Boolean = false,
    val factorScreenTime: Boolean = false
) {
    fun getDurationHoursAndMinutes(): Pair<Long, Long> {
        val durationMs = endTime - startTime
        val durationMinutes = durationMs / (1000 * 60)
        val hours = durationMinutes / 60
        val remainingMinutes = durationMinutes % 60
        return Pair(hours, remainingMinutes)
    }
}
