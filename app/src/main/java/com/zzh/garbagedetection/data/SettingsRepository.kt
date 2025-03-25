package com.zzh.garbagedetection.data

import android.content.Context
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow

class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    val settingsFlow: Flow<Preferences> = dataStore.data

    suspend fun updateSetting(key: Preferences.Key<String>, value: String) {
        dataStore.edit { prefs -> prefs[key] = value }
    }
}