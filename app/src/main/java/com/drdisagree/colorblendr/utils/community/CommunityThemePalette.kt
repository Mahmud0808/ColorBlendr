package com.drdisagree.colorblendr.utils.community

import android.util.LruCache
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme

object CommunityThemePalette {

    private val cache = LruCache<String, ResolvedScheme>(256)

    fun derive(theme: CommunityTheme, isDark: Boolean): List<List<Int>> = scheme(theme, isDark).shades

    fun scheme(theme: CommunityTheme, isDark: Boolean): ResolvedScheme {
        val key = "${theme.id}:${theme.createdAt}:$isDark"
        cache.get(key)?.let { return it }
        return ThemeEngine.scheme(source(theme), adjustments(theme, isDark), isDark)
            .also { scheme ->
                scheme.shades
                scheme.colors
                cache.put(key, scheme)
            }
    }

    private fun source(theme: CommunityTheme) = ThemeSource(
        style = theme.style,
        seedColor = theme.seedColor,
        secondaryColor = theme.secondaryColor,
        tertiaryColor = theme.tertiaryColor,
        specVersion = theme.colorSpecVersion
    )

    private fun adjustments(theme: CommunityTheme, isDark: Boolean): ThemeAdjustments {
        val lightMode = !isDark && theme.modeSpecificThemes
        return ThemeAdjustments(
            accentSaturation = if (lightMode) theme.accentSaturationLight else theme.accentSaturation,
            backgroundSaturation = if (lightMode) {
                theme.backgroundSaturationLight
            } else {
                theme.backgroundSaturation
            },
            backgroundLightness = if (lightMode) {
                theme.backgroundLightnessLight
            } else {
                theme.backgroundLightness
            },
            accurateShades = theme.accurateShades,
            pitchBlack = theme.pitchBlack,
            tintedText = theme.tintText,
            shadeOverrides = theme.colorOverrides
        )
    }
}
