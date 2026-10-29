package com.drdisagree.colorblendr.theme.override

import com.drdisagree.colorblendr.theme.token.Token

typealias TokenOverrides = ModeOverrides<Token>

typealias ResourceOverrides = ModeOverrides<String>

data class ModeOverrides<K>(
    val light: Map<K, ColorOverride> = emptyMap(),
    val dark: Map<K, ColorOverride> = emptyMap()
) {
    val isEmpty: Boolean
        get() = light.isEmpty() && dark.isEmpty()

    fun forMode(isDark: Boolean): Map<K, ColorOverride> = if (isDark) dark else light

    fun with(isDark: Boolean, key: K, override: ColorOverride?): ModeOverrides<K> {
        val updated = forMode(isDark).toMutableMap().apply {
            if (override == null) remove(key) else put(key, override)
        }
        return if (isDark) copy(dark = updated) else copy(light = updated)
    }

    fun cleared(isDark: Boolean): ModeOverrides<K> =
        if (isDark) copy(dark = emptyMap()) else copy(light = emptyMap())
}
