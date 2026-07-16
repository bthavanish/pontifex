package com.pontifex.app.di

import com.pontifex.app.data.repository.DeviceRepositoryImpl
import com.pontifex.app.data.repository.HotlistRepositoryImpl
import com.pontifex.app.data.repository.SettingsRepositoryImpl
import com.pontifex.app.domain.repository.DeviceRepository
import com.pontifex.app.domain.repository.HotlistRepository
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindHotlistRepository(impl: HotlistRepositoryImpl): HotlistRepository
}
