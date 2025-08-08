package com.drdisagree.colorblendr.data.domain

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RefreshCoordinator {

    private val _refreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshEvent = _refreshEvent.asSharedFlow()

    fun triggerRefresh() {
        _refreshEvent.tryEmit(Unit)
    }
}