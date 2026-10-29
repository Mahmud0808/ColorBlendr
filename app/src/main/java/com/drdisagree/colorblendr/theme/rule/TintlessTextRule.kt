package com.drdisagree.colorblendr.theme.rule

import android.graphics.Color
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenResolution

object TintlessTextRule : ThemeRule {

    private val SURFACE_TEXT_TOKENS = setOf(
        Token.ON_SURFACE,
        Token.ON_SURFACE_VARIANT,
        Token.ON_BACKGROUND,
        Token.ON_PRIMARY_CONTAINER,
        Token.ON_SECONDARY_CONTAINER,
        Token.ON_TERTIARY_CONTAINER,
        Token.ON_ERROR_CONTAINER
    )

    override fun token(
        token: Token,
        resolution: TokenResolution,
        isDark: Boolean
    ): TokenResolution = when (token) {
        in SURFACE_TEXT_TOKENS -> resolution.copy(color = if (isDark) Color.WHITE else Color.BLACK)
        Token.ON_ERROR -> resolution.copy(color = if (isDark) Color.BLACK else Color.WHITE)
        else -> resolution
    }
}
