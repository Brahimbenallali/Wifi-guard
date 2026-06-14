package com.wifiguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wifiguard.app.model.ConnectionEventType
import com.wifiguard.app.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAll(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE macAddress = :mac ORDER BY timestamp DESC")
    fun observeForDevice(mac: String): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(event: HistoryEntity)

    @Query("SELECT COUNT(*) FROM history WHERE macAddress = :mac AND timestamp = :timestamp AND eventType = :eventType")
    suspend fun countMatching(mac: String, timestamp: Long, eventType: ConnectionEventType): Int

    @Query("DELETE FROM history WHERE macAddress = :mac")
    suspend fun deleteForDevice(mac: String)

    @Query("SELECT COUNT(*) FROM history")
    fun observeConnectionCount(): Flow<Int>
}
