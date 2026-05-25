package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Alarm
import com.example.data.model.SleepLog
import com.example.data.model.Reminder
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        database.alarmDao(),
        database.sleepLogDao(),
        database.reminderDao()
    )

    val alarms: StateFlow<List<Alarm>> = repository.alarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sleepLogs: StateFlow<List<SleepLog>> = repository.sleepLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reminders: StateFlow<List<Reminder>> = repository.reminders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current sleep tracking session state
    private val _trackingStartTime = MutableStateFlow<Long?>(null)
    val trackingStartTime: StateFlow<Long?> = _trackingStartTime.asStateFlow()

    // Alarm Trigger Simulator State
    private val _activeTriggeredAlarm = MutableStateFlow<Alarm?>(null)
    val activeTriggeredAlarm: StateFlow<Alarm?> = _activeTriggeredAlarm.asStateFlow()

    // Active Sleep Logging (during wakeup) state flag
    private val _showWakeUpForm = MutableStateFlow(false)
    val showWakeUpForm: StateFlow<Boolean> = _showWakeUpForm.asStateFlow()

    // State of the current wake up log session
    private val _tempSleepLogStart = MutableStateFlow<Long>(0L)
    val tempSleepLogStart: StateFlow<Long> = _tempSleepLogStart.asStateFlow()

    private var hasSeeded = false

    init {
        // Seed default alarms and reminders if DB is empty
        viewModelScope.launch {
            alarms.collect { list ->
                if (!hasSeeded && list.isEmpty()) {
                    hasSeeded = true
                    seedDefaultAlarms()
                }
            }
        }
        viewModelScope.launch {
            reminders.collect { list ->
                if (list.isEmpty()) {
                    seedDefaultReminders()
                }
            }
        }

        // Periodic simulation clock to check alarm logic
        startInAppClockMonitor()
    }

    private suspend fun seedDefaultAlarms() {
        repository.insertAlarm(Alarm(hour = 7, minute = 0, label = "Despertar Diario", repeatMonday = true, repeatTuesday = true, repeatWednesday = true, repeatThursday = true, repeatFriday = true))
        repository.insertAlarm(Alarm(hour = 9, minute = 30, label = "Fin de Semana", repeatSaturday = true, repeatSunday = true))
    }

    private suspend fun seedDefaultReminders() {
        repository.insertReminder(Reminder(title = "Preparar el sueño / Apagar pantallas", hour = 22, minute = 0, type = "wind_down", repeatMonday = true, repeatTuesday = true, repeatWednesday = true, repeatThursday = true, repeatFriday = true, repeatSaturday = true, repeatSunday = true, message = "Es hora de soltar los dispositivos para relajar el cerebro."))
        repository.insertReminder(Reminder(title = "Hora de ir a la cama", hour = 22, minute = 45, type = "bedtime", repeatMonday = true, repeatTuesday = true, repeatWednesday = true, repeatThursday = true, repeatFriday = true, repeatSaturday = true, repeatSunday = true, message = "Duerme ahora para sostener tus 8 horas de descanso diario."))
        repository.insertReminder(Reminder(title = "Límitar Cafeína", hour = 14, minute = 0, type = "hydration", repeatMonday = true, repeatTuesday = true, repeatWednesday = true, repeatThursday = true, repeatFriday = true, message = "Evita la cafeína para conciliar el sueño con mayor facilidad."))
    }

    private fun startInAppClockMonitor() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10000) // Check every 10 seconds
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                // Check active alarms
                alarms.value.forEach { alarm ->
                    if (alarm.isEnabled && alarm.hour == currentHour && alarm.minute == currentMinute) {
                        // Days check
                        val isDayToRepeat = when (dayOfWeek) {
                            Calendar.MONDAY -> alarm.repeatMonday
                            Calendar.TUESDAY -> alarm.repeatTuesday
                            Calendar.WEDNESDAY -> alarm.repeatWednesday
                            Calendar.THURSDAY -> alarm.repeatThursday
                            Calendar.FRIDAY -> alarm.repeatFriday
                            Calendar.SATURDAY -> alarm.repeatSaturday
                            Calendar.SUNDAY -> alarm.repeatSunday
                            else -> false
                        }
                        // Trigger if it has no recurrence (one-time) or matched specific repeat day
                        if (!alarm.hasRecurrence() || isDayToRepeat) {
                            if (_activeTriggeredAlarm.value?.id != alarm.id) {
                                _activeTriggeredAlarm.value = alarm
                            }
                        }
                    }
                }
            }
        }
    }

    // Alarm Actions
    fun createAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.insertAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarmById(alarm.id)
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun triggerAlarmManually(alarm: Alarm) {
        _activeTriggeredAlarm.value = alarm
    }

    fun dismissActiveAlarm() {
        _activeTriggeredAlarm.value = null
    }

    fun snoozeActiveAlarm() {
        val current = _activeTriggeredAlarm.value
        if (current != null) {
            val updated = current.copy(snoozeCount = current.snoozeCount + 1)
            viewModelScope.launch {
                repository.updateAlarm(updated)
            }
            _activeTriggeredAlarm.value = null
        }
    }

    // Sleep Tracking State Mutators
    fun startSleepTracking() {
        _trackingStartTime.value = System.currentTimeMillis()
    }

    fun cancelSleepTracking() {
        _trackingStartTime.value = null
    }

    fun stopSleepTracking() {
        val start = _trackingStartTime.value
        if (start != null) {
            _tempSleepLogStart.value = start
            _showWakeUpForm.value = true
            _trackingStartTime.value = null
        }
    }

    fun saveSleepLog(
        rating: Int,
        notes: String,
        alcohol: Boolean,
        caffeine: Boolean,
        exercise: Boolean,
        stress: Boolean,
        screenTime: Boolean
    ) {
        val start = _tempSleepLogStart.value
        val end = System.currentTimeMillis()
        viewModelScope.launch {
            repository.insertSleepLog(
                SleepLog(
                    startTime = start,
                    endTime = end,
                    rating = rating,
                    notes = notes,
                    factorAlcohol = alcohol,
                    factorCaffeine = caffeine,
                    factorExercise = exercise,
                    factorStress = stress,
                    factorScreenTime = screenTime
                )
            )
            _showWakeUpForm.value = false
        }
    }

    fun discardWakeUpForm() {
        _showWakeUpForm.value = false
    }

    fun deleteSleepLog(sleepLog: SleepLog) {
        viewModelScope.launch {
            repository.deleteSleepLogById(sleepLog.id)
        }
    }

    // Custom Reminders Actions
    fun createReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isEnabled = !reminder.isEnabled))
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminderById(reminder.id)
        }
    }
}
