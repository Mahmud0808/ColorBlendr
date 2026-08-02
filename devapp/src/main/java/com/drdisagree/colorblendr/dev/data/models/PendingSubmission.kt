package com.drdisagree.colorblendr.dev.data.models

data class PendingSubmission(
    val id: String,
    val name: String,
    val author: String,
    val device: String,
    val created: Long,
    val seedColor: Int?,
    val secondaryColor: Int?,
    val tertiaryColor: Int?,
    val payloadJson: String,
    val edited: Boolean = false
) {
    companion object {
        const val MAX_NAME = 40
        const val MAX_DESCRIPTION = 500
    }
}