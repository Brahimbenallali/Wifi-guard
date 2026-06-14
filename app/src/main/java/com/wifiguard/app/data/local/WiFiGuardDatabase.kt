package com.wifiguard.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wifiguard.app.data.local.dao.AlertDao
import com.wifiguard.app.data.local.dao.DeviceDao
import com.wifiguard.app.data.local.dao.HistoryDao
import com.wifiguard.app.model.AlertEntity
import com.wifiguard.app.model.DeviceEntity
import com.wifiguard.app.model.HistoryEntity

@Database(
    entities = [DeviceEntity::class, HistoryEntity::class, AlertEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WiFiGuardDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun historyDao(): HistoryDao
    abstract fun alertDao(): AlertDao

    companion object {
        fun create(context: Context): WiFiGuardDatabase = Room.databaseBuilder(
            context,
            WiFiGuardDatabase::class.java,
            "wifi_guard.db"
        ).build()
    }
}
