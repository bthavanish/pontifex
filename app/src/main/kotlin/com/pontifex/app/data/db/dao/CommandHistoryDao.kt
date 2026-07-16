package com.pontifex.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pontifex.app.data.db.entity.CommandHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandHistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CommandHistoryEntry>>

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getBySession(sessionId: Int): Flow<List<CommandHistoryEntry>>

    @Query("SELECT * FROM command_history WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(sessionId: Int, limit: Int): List<CommandHistoryEntry>

    @Insert
    suspend fun insert(entry: CommandHistoryEntry): Long

    @Query("DELETE FROM command_history")
    suspend fun deleteAll()

    @Query("DELETE FROM command_history WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Int)
}
