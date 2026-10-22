package com.drdisagree.colorblendr.theme.overlay

object ResourceNames {

    fun pascal(tokenName: String): String = tokenName.split("_").joinToString("") { word ->
        word.replaceFirstChar { it.titlecase() }
    }

    fun mode(isDark: Boolean): String = if (isDark) "dark" else "light"
}
