package com.pontifex.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pontifex.app.data.db.entity.ConnectedDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM connected_devices ORDER BY lastSeen DESC")
    fun getAll(): Flow<List<ConnectedDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: ConnectedDevice)

    @Query("DELETE FROM connected_devices WHERE serial = :serial")
    suspend fun delete(serial: String)

    @Query("DELETE FROM connected_devices")
    suspend fun deleteAll()
}
