package com.pontifex.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pontifex.app.data.db.dao.CommandHistoryDao
import com.pontifex.app.data.db.dao.DeviceDao
import com.pontifex.app.data.db.dao.HotlistDao
import com.pontifex.app.data.db.entity.CommandHistoryEntry
import com.pontifex.app.data.db.entity.ConnectedDevice
import com.pontifex.app.data.db.entity.HotlistCommand

@Database(
    entities = [
        CommandHistoryEntry::class,
        ConnectedDevice::class,
        HotlistCommand::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandHistoryDao(): CommandHistoryDao
    abstract fun deviceDao(): DeviceDao
    abstract fun hotlistDao(): HotlistDao
}
