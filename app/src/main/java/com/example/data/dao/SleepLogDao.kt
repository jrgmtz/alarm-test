package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SleepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM sleep_logs ORDER BY endTime DESC")
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(sleepLog: SleepLog): Long

    @Update
    suspend fun updateSleepLog(sleepLog: SleepLog)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLogById(id: Long)
}
