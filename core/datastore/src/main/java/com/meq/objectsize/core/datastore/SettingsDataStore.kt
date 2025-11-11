package com.meq.objectsize.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore wrapper for app settings
 *
 * This class is part of the infrastructure layer and ONLY handles DataStore operations.
 * It knows NOTHING about domain entities (AppSettings).
 *
 * The core:data layer will map these preferences to domain entities.
 */

// Extension property for Context to get DataStore instance
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings"
)
class SettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.settingsDataStore

    /**
     * Flow of raw preferences
     * The core:data layer will map this to AppSettings
     */
    val preferences: Flow<Preferences> = dataStore.data

    /**
     * Update a preference value
     */
    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    /**
     * Clear all preferences (reset to defaults)
     */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    /**
     * Preference keys
     * Exposed so core:data can read them
     */
    object Keys {
        // Detection Settings
        val CONFIDENCE_THRESHOLD = floatPreferencesKey("confidence_threshold")
        val MAX_OBJECTS = intPreferencesKey("max_objects")
        val SAME_PLANE_THRESHOLD = floatPreferencesKey("same_plane_threshold")

        // Camera Settings
        val TARGET_RESOLUTION_WIDTH = intPreferencesKey("target_resolution_width")
        val TARGET_RESOLUTION_HEIGHT = intPreferencesKey("target_resolution_height")
        val FRAME_CAPTURE_INTERVAL_MS = longPreferencesKey("frame_capture_interval_ms")

        // ML Settings
        val ENABLE_GPU_DELEGATE = booleanPreferencesKey("enable_gpu_delegate")
        val NUM_THREADS = intPreferencesKey("num_threads")

        // Performance Settings
        val SHOW_PERFORMANCE_OVERLAY = booleanPreferencesKey("show_performance_overlay")
        val PERFORMANCE_REFRESH_RATE = longPreferencesKey("performance_refresh_rate")

        // Reference Object Sizes (in cm)
        val CELL_PHONE_WIDTH = floatPreferencesKey("cell_phone_width")
        val CELL_PHONE_HEIGHT = floatPreferencesKey("cell_phone_height")
        val BOOK_WIDTH = floatPreferencesKey("book_width")
        val BOOK_HEIGHT = floatPreferencesKey("book_height")
        val BOTTLE_WIDTH = floatPreferencesKey("bottle_width")
        val BOTTLE_HEIGHT = floatPreferencesKey("bottle_height")
        val CUP_WIDTH = floatPreferencesKey("cup_width")
        val CUP_HEIGHT = floatPreferencesKey("cup_height")
        val KEYBOARD_WIDTH = floatPreferencesKey("keyboard_width")
        val KEYBOARD_HEIGHT = floatPreferencesKey("keyboard_height")
    }
}
