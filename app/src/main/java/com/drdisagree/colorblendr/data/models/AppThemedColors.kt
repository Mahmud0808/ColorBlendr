package com.drdisagree.colorblendr.data.models

data class AppThemedColors(
    val light: Map<String, Int> = emptyMap(),
    val dark: Map<String, Int> = emptyMap(),
    val presetNames: Set<String> = emptySet()
) {
    fun forMode(isDark: Boolean): Map<String, Int> = if (isDark) dark else light
}
