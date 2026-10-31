package com.drdisagree.colorblendr.theme.rule

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenResolution

object TintlessTextRule : ThemeRule {

    private const val MID_LIGHTNESS = 50.0

    private val TEXT_TOKENS = setOf(
        Token.ON_SURFACE,
        Token.ON_SURFACE_VARIANT,
        Token.ON_BACKGROUND,
        Token.ON_PRIMARY_CONTAINER,
        Token.ON_SECONDARY_CONTAINER,
        Token.ON_TERTIARY_CONTAINER,
        Token.ON_ERROR_CONTAINER,
        Token.ON_ERROR
    )

    override fun token(
        token: Token,
        resolution: TokenResolution,
        isDark: Boolean
    ): TokenResolution {
        if (token !in TEXT_TOKENS) return resolution

        val original = resolution.color
        val light = original?.let(::lightness)?.let { it >= MID_LIGHTNESS }
            ?: (resolution.tone >= MID_LIGHTNESS)
        val alpha = original?.let(Color::alpha) ?: 255
        return resolution.copy(
            color = ColorUtils.setAlphaComponent(if (light) Color.WHITE else Color.BLACK, alpha)
        )
    }

    private fun lightness(color: Int): Double =
        DoubleArray(3).also { ColorUtils.colorToLAB(color, it) }[0]
}
