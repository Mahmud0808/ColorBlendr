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
import com.drdisagree.colorblendr.dev.data.models.PreviewResult
import com.drdisagree.colorblendr.dev.data.models.StackedMessage
import com.drdisagree.colorblendr.dev.utils.ThemeForwarder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
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
        val key = rawKey.filterNot(Char::isISOControl).trim()
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
                DevPrefs.setLastCheck(getApplication(), System.currentTimeMillis())
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
        val message = when (ThemeForwarder.openPreview(getApplication(), item.payloadJson)) {
            PreviewResult.SUCCESS -> null
            PreviewResult.NOT_INSTALLED -> R.string.colorblendr_not_installed
            PreviewResult.SIGNATURE_MISMATCH -> R.string.error_preview_signature
            PreviewResult.NO_ACTIVITY -> R.string.error_preview_no_activity
            PreviewResult.ERROR -> R.string.error_preview_generic
        }
        if (message != null) push(string(message))
    }

    fun approve(item: PendingSubmission) {
        _pending.update { it?.filterNot { s -> s.id == item.id } }
        val job = viewModelScope.launch {
            delay(UNDO_DELAY)
            when (val result = AdminApi.approve(savedKey, item.id)) {
                is ApiResult.Success -> Unit
                is ApiResult.Failure -> {
                    restorePending(listOf(item))
                    push(failureMessage(result))
                }
            }
        }
        pushUndo(string(R.string.theme_approved)) {
            job.cancel()
            restorePending(listOf(item))
        }
    }

    fun reject(item: PendingSubmission) {
        _pending.update { it?.filterNot { s -> s.id == item.id } }
        val job = viewModelScope.launch {
            delay(UNDO_DELAY)
            when (val result = AdminApi.reject(savedKey, item.id)) {
                is ApiResult.Success -> Unit
                is ApiResult.Failure -> {
                    restorePending(listOf(item))
                    push(failureMessage(result))
                }
            }
        }
        pushUndo(string(R.string.theme_rejected)) {
            job.cancel()
            restorePending(listOf(item))
        }
    }

    fun block(item: PendingSubmission, reason: String) {
        val removed = _pending.value?.filter { it.device == item.device } ?: emptyList()
        val entry = BlockedEntry(
            device = item.device,
            reason = reason,
            created = System.currentTimeMillis()
        )
        _pending.update { it?.filterNot { s -> s.device == item.device } }
        _blocked.update { (it ?: emptyList()) + entry }
        val job = viewModelScope.launch {
            delay(UNDO_DELAY)
            when (val result = AdminApi.block(savedKey, item.device, reason)) {
                is ApiResult.Success -> Unit
                is ApiResult.Failure -> {
                    _blocked.update { it?.filterNot { b -> b.device == entry.device } }
                    restorePending(removed)
                    push(failureMessage(result))
                }
            }
        }
        pushUndo(string(R.string.uploader_blocked)) {
            job.cancel()
            _blocked.update { it?.filterNot { b -> b.device == entry.device } }
            restorePending(removed)
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
                    push(string(R.string.uploader_unblocked))
                }

                is ApiResult.Failure -> push(failureMessage(result))
            }
        }
    }

    fun approveAll(items: List<PendingSubmission>) {
        if (items.isEmpty()) return
        _busy.value = true
        viewModelScope.launch {
            var ok = 0
            var failed = 0
            items.forEach { item ->
                when (AdminApi.approve(savedKey, item.id)) {
                    is ApiResult.Success -> {
                        _pending.update { it?.filterNot { s -> s.id == item.id } }
                        ok++
                    }

                    is ApiResult.Failure -> failed++
                }
            }
            _busy.value = false
            push(bulkResult(R.string.bulk_approved, ok, failed))
        }
    }

    fun rejectAll(items: List<PendingSubmission>) {
        if (items.isEmpty()) return
        _busy.value = true
        viewModelScope.launch {
            var ok = 0
            var failed = 0
            items.forEach { item ->
                when (AdminApi.reject(savedKey, item.id)) {
                    is ApiResult.Success -> {
                        _pending.update { it?.filterNot { s -> s.id == item.id } }
                        ok++
                    }

                    is ApiResult.Failure -> failed++
                }
            }
            _busy.value = false
            push(bulkResult(R.string.bulk_rejected, ok, failed))
        }
    }

    fun blockAll(items: List<PendingSubmission>, reason: String) {
        if (items.isEmpty()) return
        _busy.value = true
        viewModelScope.launch {
            var ok = 0
            var failed = 0
            items.distinctBy { it.device }.forEach { item ->
                when (AdminApi.block(savedKey, item.device, reason)) {
                    is ApiResult.Success -> {
                        _pending.update { it?.filterNot { s -> s.device == item.device } }
                        _blocked.update {
                            (it ?: emptyList()) + BlockedEntry(
                                device = item.device,
                                reason = reason,
                                created = System.currentTimeMillis()
                            )
                        }
                        ok++
                    }

                    is ApiResult.Failure -> failed++
                }
            }
            _busy.value = false
            push(bulkResult(R.string.bulk_blocked, ok, failed))
        }
    }

    fun dismissMessage(id: Long) {
        _messages.update { it.filterNot { m -> m.id == id } }
    }

    private fun restorePending(items: List<PendingSubmission>) {
        if (items.isEmpty()) return
        _pending.update { current ->
            val existing = current ?: emptyList()
            val known = existing.map { it.id }.toSet()
            existing + items.filterNot { it.id in known }
        }
    }

    private fun bulkResult(templateRes: Int, ok: Int, failed: Int): String {
        val base = string(templateRes, ok)
        return if (failed > 0) base + string(R.string.bulk_failed, failed) else base
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

    private fun string(res: Int, vararg args: Any): String =
        getApplication<Application>().getString(res, *args)

    private fun push(text: String) {
        _messages.update {
            it + StackedMessage(id = System.nanoTime(), text = text.trim())
        }
    }

    private fun pushUndo(text: String, onUndo: () -> Unit) {
        val id = System.nanoTime()
        _messages.update {
            it + StackedMessage(
                id = id,
                text = text.trim(),
                actionLabel = string(R.string.undo),
                onAction = {
                    onUndo()
                    dismissMessage(id)
                },
                durationMillis = UNDO_DELAY
            )
        }
    }

    private companion object {
        const val UNDO_DELAY = 4000L
    }
}
