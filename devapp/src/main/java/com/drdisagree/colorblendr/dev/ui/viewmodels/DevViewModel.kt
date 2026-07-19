package com.drdisagree.colorblendr.dev.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.api.AdminApi
import com.drdisagree.colorblendr.dev.data.api.ApiResult
import com.drdisagree.colorblendr.dev.data.config.DevPrefs
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.data.models.StackedMessage
import com.drdisagree.colorblendr.dev.utils.ThemeForwarder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DevViewModel(application: Application) : AndroidViewModel(application) {

    private val _authorized = MutableStateFlow(false)
    val authorized: StateFlow<Boolean> = _authorized.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _pending = MutableStateFlow<List<PendingSubmission>?>(null)
    val pending: StateFlow<List<PendingSubmission>?> = _pending.asStateFlow()

    private val _blocked = MutableStateFlow<List<BlockedEntry>?>(null)
    val blocked: StateFlow<List<BlockedEntry>?> = _blocked.asStateFlow()

    private val _messages = MutableStateFlow(listOf<StackedMessage>())
    val messages: StateFlow<List<StackedMessage>> = _messages.asStateFlow()

    var savedKey: String = DevPrefs.adminKey(application)
        private set

    fun refresh(rawKey: String = savedKey) {
        val key = rawKey.trim()
        if (key.isEmpty()) return
        _loading.value = true
        viewModelScope.launch {
            val fetchedPending = AdminApi.fetchPending(key)
            val fetchedBlocked = AdminApi.fetchBlocked(key)
            _loading.value = false
            val failure = (fetchedPending as? ApiResult.Failure)
                ?: (fetchedBlocked as? ApiResult.Failure)
            if (failure != null) {
                if (_pending.value == null && _blocked.value == null) {
                    _authorized.value = false
                }
                push(failureMessage(failure))
            } else {
                _authorized.value = true
                savedKey = key
                DevPrefs.setAdminKey(getApplication(), key)
                _pending.value = (fetchedPending as ApiResult.Success).data
                _blocked.value = (fetchedBlocked as ApiResult.Success).data
            }
        }
    }

    fun logout() {
        DevPrefs.clearAdminKey(getApplication())
        savedKey = ""
        _authorized.value = false
        _pending.value = null
        _blocked.value = null
    }

    fun openPreview(item: PendingSubmission) {
        if (!ThemeForwarder.openPreview(getApplication(), item.payloadJson)) {
            push(getApplication<Application>().getString(R.string.colorblendr_not_installed))
        }
    }

    fun approve(item: PendingSubmission) {
        _busy.value = true
        viewModelScope.launch {
            val result = AdminApi.approve(savedKey, item.id)
            _busy.value = false
            when (result) {
                is ApiResult.Success -> {
                    _pending.update { it?.filterNot { s -> s.id == item.id } }
                    push(getApplication<Application>().getString(R.string.theme_approved))
                }

                is ApiResult.Failure -> push(failureMessage(result))
            }
        }
    }

    fun reject(item: PendingSubmission) {
        _busy.value = true
        viewModelScope.launch {
            val result = AdminApi.reject(savedKey, item.id)
            _busy.value = false
            when (result) {
                is ApiResult.Success -> {
                    _pending.update { it?.filterNot { s -> s.id == item.id } }
                    push(getApplication<Application>().getString(R.string.theme_rejected))
                }

                is ApiResult.Failure -> push(failureMessage(result))
            }
        }
    }

    fun block(item: PendingSubmission, reason: String) {
        _busy.value = true
        viewModelScope.launch {
            val result = AdminApi.block(savedKey, item.device, reason)
            _busy.value = false
            when (result) {
                is ApiResult.Success -> {
                    _pending.update { it?.filterNot { s -> s.device == item.device } }
                    _blocked.update {
                        it?.plus(
                            BlockedEntry(
                                device = item.device,
                                reason = reason,
                                created = System.currentTimeMillis()
                            )
                        )
                    }
                    push(getApplication<Application>().getString(R.string.uploader_blocked))
                }

                is ApiResult.Failure -> push(failureMessage(result))
            }
        }
    }

    fun unblock(entry: BlockedEntry) {
        _busy.value = true
        viewModelScope.launch {
            val result = AdminApi.unblock(savedKey, entry.device)
            _busy.value = false
            when (result) {
                is ApiResult.Success -> {
                    _blocked.update { it?.filterNot { b -> b.device == entry.device } }
                    push(getApplication<Application>().getString(R.string.uploader_unblocked))
                }

                is ApiResult.Failure -> push(failureMessage(result))
            }
        }
    }

    fun dismissMessage(id: Long) {
        _messages.update { it.filterNot { m -> m.id == id } }
    }

    private fun failureMessage(failure: ApiResult.Failure): String {
        val app = getApplication<Application>()
        return when (failure.code) {
            null -> app.getString(R.string.error_network, failure.error ?: "")
            401 -> app.getString(R.string.error_unauthorized)
            429 -> app.getString(R.string.error_locked_out)
            502 -> app.getString(R.string.error_github)
            else -> app.getString(
                R.string.error_generic,
                failure.code,
                failure.error ?: app.getString(R.string.error_unknown)
            )
        }
    }

    private fun push(text: String) {
        _messages.update {
            it + StackedMessage(id = System.nanoTime(), text = text.trim())
        }
    }
}