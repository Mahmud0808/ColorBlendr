package com.drdisagree.colorblendr.theme.override

import com.drdisagree.colorblendr.theme.token.Token

data class TokenOverrides(
    val light: Map<Token, TokenOverride> = emptyMap(),
    val dark: Map<Token, TokenOverride> = emptyMap()
) {
    val isEmpty: Boolean
        get() = light.isEmpty() && dark.isEmpty()

    fun forMode(isDark: Boolean): Map<Token, TokenOverride> = if (isDark) dark else light

    fun with(isDark: Boolean, token: Token, override: TokenOverride?): TokenOverrides {
        val updated = forMode(isDark).toMutableMap().apply {
            if (override == null) remove(token) else put(token, override)
        }
        return if (isDark) copy(dark = updated) else copy(light = updated)
    }

    fun cleared(isDark: Boolean): TokenOverrides =
        if (isDark) copy(dark = emptyMap()) else copy(light = emptyMap())
}
