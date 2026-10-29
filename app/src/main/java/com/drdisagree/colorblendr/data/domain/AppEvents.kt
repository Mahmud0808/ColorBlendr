package com.drdisagree.colorblendr.data.domain

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// In-process event bus keyed by intent action strings; replaces the
// deprecated LocalBroadcastManager relay between BroadcastListener and
// screens.
object AppEvents {

    private val _events = MutableSharedFlow<String>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val _packageChanges = MutableSharedFlow<String>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val packageChanges = _packageChanges.asSharedFlow()

    fun emit(action: String) {
        _events.tryEmit(action)
    }

    fun emitPackageChanged(packageName: String) {
        _packageChanges.tryEmit(packageName)
    }
}
