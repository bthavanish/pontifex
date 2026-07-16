package com.pontifex.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "connected_devices",
    indices = [Index(value = ["serial"], unique = true)]
)
data class ConnectedDevice(
    @PrimaryKey val serial: String,
    val name: String,
    val connectionType: String,
    val state: String,
    val lastSeen: Long = System.currentTimeMillis()
)
