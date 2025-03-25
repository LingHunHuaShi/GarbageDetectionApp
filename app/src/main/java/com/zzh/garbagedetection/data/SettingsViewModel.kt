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

    fun setModelName(modelName: String) = viewModelScope.launch {
        repo.updateSetting(SettingsPreferences.KEY_MODEL_NAME, modelName)
    }
}