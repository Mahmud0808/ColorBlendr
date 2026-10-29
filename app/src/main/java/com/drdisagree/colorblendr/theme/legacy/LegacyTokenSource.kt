package com.drdisagree.colorblendr.theme.legacy

import com.drdisagree.colorblendr.theme.model.PaletteRole

data class LegacyTokenSource(
    val role: PaletteRole,
    val lightIndex: Int,
    val darkIndex: Int,
    val lightAdjustment: Int = 0,
    val darkAdjustment: Int = 0
) {
    fun index(isDark: Boolean): Int = if (isDark) darkIndex else lightIndex

    fun adjustment(isDark: Boolean): Int = if (isDark) darkAdjustment else lightAdjustment
}
