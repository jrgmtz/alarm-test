package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "Alarma",
    val repeatMonday: Boolean = false,
    val repeatTuesday: Boolean = false,
    val repeatWednesday: Boolean = false,
    val repeatThursday: Boolean = false,
    val repeatFriday: Boolean = false,
    val repeatSaturday: Boolean = false,
    val repeatSunday: Boolean = false,
    val isVibrate: Boolean = true,
    val snoozeCount: Int = 0
) {
    fun hasRecurrence(): Boolean {
        return repeatMonday || repeatTuesday || repeatWednesday || repeatThursday || repeatFriday || repeatSaturday || repeatSunday
    }
    
    fun getRecurrenceString(): String {
        if (!hasRecurrence()) return "Una vez"
        val days = mutableListOf<String>()
        if (repeatMonday && repeatTuesday && repeatWednesday && repeatThursday && repeatFriday && repeatSaturday && repeatSunday) {
            return "Todos los días"
        }
        if (repeatMonday && repeatTuesday && repeatWednesday && repeatThursday && repeatFriday && !repeatSaturday && !repeatSunday) {
            return "Lunes a Viernes"
        }
        if (!repeatMonday && !repeatTuesday && !repeatWednesday && !repeatThursday && !repeatFriday && repeatSaturday && repeatSunday) {
            return "Fines de semana"
        }
        if (repeatMonday) days.add("Lun")
        if (repeatTuesday) days.add("Mar")
        if (repeatWednesday) days.add("Mié")
        if (repeatThursday) days.add("Jue")
        if (repeatFriday) days.add("Vie")
        if (repeatSaturday) days.add("Sáb")
        if (repeatSunday) days.add("Dom")
        return days.joinToString(", ")
    }
}
