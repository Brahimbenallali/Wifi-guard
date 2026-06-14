package com.wifiguard.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.DeviceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    fun observeDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY lastSeen DESC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE macAddress = :mac LIMIT 1")
    suspend fun getDevice(mac: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE macAddress = :mac LIMIT 1")
    fun observeDevice(mac: String): Flow<DeviceEntity?>

    @Upsert
    suspend fun upsert(device: DeviceEntity)

    @Query("UPDATE devices SET isOnline = 0 WHERE isOnline = 1 AND macAddress NOT IN (:onlineMacs)")
    suspend fun markMissingOffline(onlineMacs: List<String>)

    @Query("UPDATE devices SET isOnline = 0 WHERE isOnline = 1")
    suspend fun markAllOffline()

    @Query("UPDATE devices SET status = :status WHERE macAddress = :mac")
    suspend fun updateStatus(mac: String, status: DeviceStatus)

    @Query("UPDATE devices SET name = :name WHERE macAddress = :mac")
    suspend fun rename(mac: String, name: String)

    @Query("DELETE FROM devices WHERE macAddress = :mac")
    suspend fun delete(mac: String)
}
