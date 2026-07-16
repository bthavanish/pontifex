package com.pontifex.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hotlist_commands",
    indices = [Index(value = ["category"])]
)
data class HotlistCommand(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val command: String,
    val description: String = "",
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)
