package com.pontifex.app.domain.repository

import com.pontifex.app.data.db.entity.HotlistCommand
import kotlinx.coroutines.flow.Flow

interface HotlistRepository {
    fun getCommands(): Flow<List<HotlistCommand>>
    suspend fun addCommand(command: HotlistCommand)
    suspend fun removeCommand(id: Long)
    suspend fun updateCommand(command: HotlistCommand)
}
