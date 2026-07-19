package com.drdisagree.colorblendr.dev.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.api.AdminApi
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
            if (fetchedPending == null || fetchedBlocked == null) {
                if (_pending.value == null && _blocked.value == null) {
                    _authorized.value = false
                }
                push(R.string.request_failed)
            } else {
                _authorized.value = true
                savedKey = key
                DevPrefs.setAdminKey(getApplication(), key)
                _pending.value = fetchedPending
                _blocked.value = fetchedBlocked
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
            push(R.string.colorblendr_not_installed)
        }
    }

    fun approve(item: PendingSubmission) {
        _busy.value = true
        viewModelScope.launch {
            val prUrl = AdminApi.approve(savedKey, item.id)
            _busy.value = false
            if (prUrl != null) {
                _pending.update { it?.filterNot { s -> s.id == item.id } }
                push(R.string.theme_approved)
            } else {
                push(R.string.request_failed)
            }
        }
    }

    fun reject(item: PendingSubmission) {
        _busy.value = true
        viewModelScope.launch {
            val rejected = AdminApi.reject(savedKey, item.id)
            _busy.value = false
            if (rejected) {
                _pending.update { it?.filterNot { s -> s.id == item.id } }
                push(R.string.theme_rejected)
            } else {
                push(R.string.request_failed)
            }
        }
    }

    fun block(item: PendingSubmission, reason: String) {
        _busy.value = true
        viewModelScope.launch {
            val blocked = AdminApi.block(savedKey, item.device, reason)
            _busy.value = false
            if (blocked) {
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
                push(R.string.uploader_blocked)
            } else {
                push(R.string.request_failed)
            }
        }
    }

    fun unblock(entry: BlockedEntry) {
        _busy.value = true
        viewModelScope.launch {
            val unblocked = AdminApi.unblock(savedKey, entry.device)
            _busy.value = false
            if (unblocked) {
                _blocked.update { it?.filterNot { b -> b.device == entry.device } }
                push(R.string.uploader_unblocked)
            } else {
                push(R.string.request_failed)
            }
        }
    }

    fun dismissMessage(id: Long) {
        _messages.update { it.filterNot { m -> m.id == id } }
    }

    private fun push(resId: Int) {
        _messages.update {
            it + StackedMessage(
                id = System.nanoTime(),
                text = getApplication<Application>().getString(resId)
            )
        }
    }
}