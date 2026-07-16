package com.pontifex.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotlist_commands")
data class HotlistCommand(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val command: String,
    val description: String = "",
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)
