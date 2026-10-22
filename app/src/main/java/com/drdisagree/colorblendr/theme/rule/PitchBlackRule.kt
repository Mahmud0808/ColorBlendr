package com.drdisagree.colorblendr.theme.rule

import android.graphics.Color
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenResolution

class PitchBlackRule(private val surfaceTone: Double) : ThemeRule {

    override fun token(
        token: Token,
        resolution: TokenResolution,
        isDark: Boolean
    ): TokenResolution {
        if (!isDark || resolution.role?.isNeutral != true || token !in SURFACE_TOKENS) {
            return resolution
        }
        return resolution.copy(tone = darken(resolution.tone))
    }

    override fun shade(role: PaletteRole, index: Int, color: Int, isDark: Boolean): Int =
        if (isDark && role.isNeutral && index == ShadeScale.SHADE_900) Color.BLACK else color

    override fun referenceTone(role: PaletteRole, tone: Double, isDark: Boolean): Double =
        if (isDark && role.isNeutral && tone in REFERENCE_SURFACE_TONES) darken(tone) else tone

    private fun darken(tone: Double): Double = (tone - surfaceTone).coerceAtLeast(0.0) * SPREAD

    private companion object {
        const val SPREAD = 1.15

        val SURFACE_TOKENS = setOf(
            Token.BACKGROUND,
            Token.SURFACE,
            Token.SURFACE_DIM,
            Token.SURFACE_BRIGHT,
            Token.SURFACE_CONTAINER_LOWEST,
            Token.SURFACE_CONTAINER_LOW,
            Token.SURFACE_CONTAINER,
            Token.SURFACE_CONTAINER_HIGH,
            Token.SURFACE_CONTAINER_HIGHEST
        )

        val REFERENCE_SURFACE_TONES = setOf(4.0, 6.0, 12.0, 17.0, 22.0, 24.0)
    }
}
