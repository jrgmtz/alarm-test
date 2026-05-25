package com.example.data.repository

import com.example.data.dao.AlarmDao
import com.example.data.dao.SleepLogDao
import com.example.data.dao.ReminderDao
import com.example.data.model.Alarm
import com.example.data.model.SleepLog
import com.example.data.model.Reminder
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val alarmDao: AlarmDao,
    private val sleepLogDao: SleepLogDao,
    private val reminderDao: ReminderDao
) {
    val alarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val sleepLogs: Flow<List<SleepLog>> = sleepLogDao.getAllSleepLogs()
    val reminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun insertSleepLog(sleepLog: SleepLog): Long = sleepLogDao.insertSleepLog(sleepLog)
    suspend fun updateSleepLog(sleepLog: SleepLog) = sleepLogDao.updateSleepLog(sleepLog)
    suspend fun deleteSleepLogById(id: Long) = sleepLogDao.deleteSleepLogById(id)

    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)
}
