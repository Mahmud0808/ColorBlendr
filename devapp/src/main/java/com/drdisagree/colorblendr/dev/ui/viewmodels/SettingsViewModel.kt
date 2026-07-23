package com.drdisagree.colorblendr.dev.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.drdisagree.colorblendr.dev.data.config.DevPrefs
import com.drdisagree.colorblendr.dev.utils.PendingCheckScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _notifyEnabled = MutableStateFlow(DevPrefs.notifyEnabled(application))
    val notifyEnabled: StateFlow<Boolean> = _notifyEnabled.asStateFlow()

    private val _notifyInterval = MutableStateFlow(DevPrefs.notifyIntervalHours(application))
    val notifyInterval: StateFlow<Int> = _notifyInterval.asStateFlow()

    fun setNotifyEnabled(enabled: Boolean) {
        DevPrefs.setNotifyEnabled(getApplication(), enabled)
        _notifyEnabled.value = enabled
        PendingCheckScheduler.sync(getApplication())
    }

    fun setNotifyInterval(hours: Int) {
        DevPrefs.setNotifyIntervalHours(getApplication(), hours)
        _notifyInterval.value = hours
        PendingCheckScheduler.sync(getApplication())
    }
}