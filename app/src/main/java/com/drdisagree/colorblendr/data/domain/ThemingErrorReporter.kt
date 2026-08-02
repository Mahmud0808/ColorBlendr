package com.drdisagree.colorblendr.data.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Surfaces theming failures as one dialog: first error wins until dismissed,
// so a burst of failures (e.g. every overlay of one apply) shows once.
object ThemingErrorReporter {

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun report(message: String) {
        _error.compareAndSet(null, message)
    }

    fun dismiss() {
        _error.value = null
    }
}
