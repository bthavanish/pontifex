package com.pontifex.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pontifex.app.data.db.entity.HotlistCommand
import kotlinx.coroutines.flow.Flow

@Dao
interface HotlistDao {
    @Query("SELECT * FROM hotlist_commands ORDER BY category, name")
    fun getAll(): Flow<List<HotlistCommand>>

    @Query("SELECT * FROM hotlist_commands WHERE category = :category ORDER BY name")
    fun getByCategory(category: String): Flow<List<HotlistCommand>>

    @Insert
    suspend fun insert(command: HotlistCommand): Long

    @Update
    suspend fun update(command: HotlistCommand)

    @Delete
    suspend fun delete(command: HotlistCommand)

    @Query("DELETE FROM hotlist_commands WHERE id = :id")
    suspend fun deleteById(id: Long)
}
