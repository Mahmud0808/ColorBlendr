package com.drdisagree.colorblendr.dev.data.models

data class BlockedEntry(
    val device: String,
    val reason: String,
    val created: Long
)