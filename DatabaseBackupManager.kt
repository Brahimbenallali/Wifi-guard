package com.wifiguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wifiguard.app.model.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun observeAlerts(): Flow<List<AlertEntity>>

    @Insert
    suspend fun insert(alert: AlertEntity)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllRead()
}
