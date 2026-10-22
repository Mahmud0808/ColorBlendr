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
        val key = "${key(theme)}:$isDark"
        cache.get(key)?.let { return it }
        val adjustments = adjustments(theme, isDark).copy(
            tokenOverrides = theme.tokenOverrides,
            legacyTokens = usesLegacyTokens(theme)
        )
        return ThemeEngine.scheme(source(theme), adjustments, isDark)
            .also { scheme ->
                scheme.shades
                scheme.colors
                cache.put(key, scheme)
            }
    }

    fun usesLegacyTokens(theme: CommunityTheme): Boolean =
        theme.schemaVersion == CommunityThemeCodec.SCHEMA_VERSION &&
                theme.engineVersion < CommunityThemeCodec.ENGINE_VERSION_TOKENS &&
                theme.colorOverrides.isNotEmpty()

    private fun key(theme: CommunityTheme) = "${theme.id}:${theme.createdAt}"

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
            pitchBlackContainers = theme.pitchBlackContainers,
            tintedText = theme.tintText,
            shadeOverrides = theme.colorOverrides
        )
    }
}
