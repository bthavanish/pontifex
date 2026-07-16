package com.pontifex.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val command: String,
    val sessionId: Int,
    val timestamp: Long = System.currentTimeMillis()
)
