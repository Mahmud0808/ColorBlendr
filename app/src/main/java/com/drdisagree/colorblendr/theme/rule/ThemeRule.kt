package com.drdisagree.colorblendr.theme.rule

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenResolution

interface ThemeRule {

    fun token(token: Token, resolution: TokenResolution, isDark: Boolean): TokenResolution =
        resolution

    @ColorInt
    fun shade(role: PaletteRole, index: Int, @ColorInt color: Int, isDark: Boolean): Int = color

    fun referenceTone(role: PaletteRole, tone: Double, isDark: Boolean): Double = tone
}
