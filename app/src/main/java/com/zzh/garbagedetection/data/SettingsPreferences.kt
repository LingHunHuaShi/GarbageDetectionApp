package com.zzh.garbagedetection.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_PREFS_KEY = "settings_prefs"
val Context.dataStore by preferencesDataStore(SETTINGS_PREFS_KEY)

object SettingsPreferences {
    val KEY_MODEL_NAME = stringPreferencesKey("model_name")
}