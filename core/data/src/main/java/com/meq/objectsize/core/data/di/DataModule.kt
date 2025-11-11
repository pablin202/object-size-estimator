package com.meq.objectsize.core.data.di

import com.meq.objectsize.core.data.repository.SettingsRepositoryImpl
import com.meq.objectsize.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer
 *
 * Binds implementations to domain interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * Bind SettingsRepository implementation
     *
     * This connects the domain interface (SettingsRepository)
     * to the infrastructure implementation (SettingsRepositoryImpl)
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
