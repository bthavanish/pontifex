package com.pontifex.app.data.repository

import com.pontifex.app.data.db.dao.HotlistDao
import com.pontifex.app.data.db.entity.HotlistCommand
import com.pontifex.app.domain.repository.HotlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotlistRepositoryImpl @Inject constructor(
    private val hotlistDao: HotlistDao
) : HotlistRepository {
    override fun getCommands(): Flow<List<HotlistCommand>> =
        hotlistDao.getAll()

    override suspend fun addCommand(command: HotlistCommand) {
        hotlistDao.insert(command)
    }

    override suspend fun removeCommand(id: Long) {
        hotlistDao.deleteById(id)
    }

    override suspend fun updateCommand(command: HotlistCommand) {
        hotlistDao.update(command)
    }
}
