package com.drdisagree.colorblendr.dev.data.models

data class StackedMessage(
    val id: Long,
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMillis: Long = 3500L
)
