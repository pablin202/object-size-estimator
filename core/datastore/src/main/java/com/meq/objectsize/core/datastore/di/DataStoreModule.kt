package com.meq.objectsize.core.datastore.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for DataStore dependencies
 *
 * This module is currently empty because SettingsDataStore
 * uses @Inject constructor and Hilt can create it automatically.
 *
 * This module exists for future DataStore-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    // No @Provides needed - SettingsDataStore uses @Inject constructor
}
