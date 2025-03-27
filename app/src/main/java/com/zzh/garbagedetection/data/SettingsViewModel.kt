package com.zzh.garbagedetection.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = SettingsRepository(application)

    val modelName = repo.settingsFlow.map { it[SettingsPreferences.KEY_MODEL_NAME] ?: ModelNameEnums.GOOGLE.label }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ModelNameEnums.GOOGLE.label)

    val threshold = repo.settingsFlow.map { it[SettingsPreferences.KEY_THRESHOLD] ?: 0.5f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.5f)

    fun setModelName(modelName: String) = viewModelScope.launch {
        repo.updateSetting(SettingsPreferences.KEY_MODEL_NAME, modelName)
    }

    fun setThreshold(threshold: Float) = viewModelScope.launch {
        repo.updateSetting(SettingsPreferences.KEY_THRESHOLD, threshold)
    }
}