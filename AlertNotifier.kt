package com.wifiguard.app.data.local

import androidx.room.TypeConverter
import com.wifiguard.app.model.AlertType
import com.wifiguard.app.model.ConnectionEventType
import com.wifiguard.app.model.DeviceStatus

class Converters {
    @TypeConverter fun toDeviceStatus(value: String) = DeviceStatus.valueOf(value)
    @TypeConverter fun fromDeviceStatus(value: DeviceStatus) = value.name
    @TypeConverter fun toEventType(value: String) = ConnectionEventType.valueOf(value)
    @TypeConverter fun fromEventType(value: ConnectionEventType) = value.name
    @TypeConverter fun toAlertType(value: String) = AlertType.valueOf(value)
    @TypeConverter fun fromAlertType(value: AlertType) = value.name
}
