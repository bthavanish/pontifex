package com.pontifex.app.di

import android.content.Context
import androidx.room.Room
import com.pontifex.app.data.db.AppDatabase
import com.pontifex.app.data.db.dao.CommandHistoryDao
import com.pontifex.app.data.db.dao.DeviceDao
import com.pontifex.app.data.db.dao.HotlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pontifex.db"
        ).build()
    }

    @Provides
    fun provideCommandHistoryDao(database: AppDatabase): CommandHistoryDao {
        return database.commandHistoryDao()
    }

    @Provides
    fun provideDeviceDao(database: AppDatabase): DeviceDao {
        return database.deviceDao()
    }

    @Provides
    fun provideHotlistDao(database: AppDatabase): HotlistDao {
        return database.hotlistDao()
    }
}
