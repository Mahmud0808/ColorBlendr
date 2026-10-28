package com.drdisagree.colorblendr.utils.community

import android.util.LruCache
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.data.enums.ThemeModeSupport
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenContrast

object CommunityThemePalette {

    private const val MIN_TEXT_CONTRAST = 1.5
    private const val MIN_ACCENT_CONTRAST = 1.15

    private val READABILITY_PAIRS = listOf(
        Triple(Token.ON_SURFACE, Token.SURFACE, MIN_TEXT_CONTRAST),
        Triple(Token.ON_SURFACE_VARIANT, Token.SURFACE, MIN_TEXT_CONTRAST),
        Triple(Token.ON_SURFACE, Token.SURFACE_CONTAINER, MIN_TEXT_CONTRAST),
        Triple(Token.ON_SURFACE_VARIANT, Token.SURFACE_CONTAINER, MIN_TEXT_CONTRAST),
        Triple(Token.ON_SURFACE, Token.SURFACE_CONTAINER_HIGH, MIN_TEXT_CONTRAST),
        Triple(Token.ON_PRIMARY, Token.PRIMARY, MIN_ACCENT_CONTRAST),
        Triple(Token.ON_PRIMARY_CONTAINER, Token.PRIMARY_CONTAINER, MIN_ACCENT_CONTRAST),
        Triple(Token.ON_SECONDARY_CONTAINER, Token.SECONDARY_CONTAINER, MIN_ACCENT_CONTRAST),
        Triple(Token.PRIMARY, Token.SURFACE, MIN_ACCENT_CONTRAST)
    )

    private val cache = LruCache<String, ResolvedScheme>(256)
    private val modeSupportCache = LruCache<String, ThemeModeSupport>(256)
    private val lowContrastCache = LruCache<String, Boolean>(256)

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

    fun modeSupport(theme: CommunityTheme): ThemeModeSupport {
        val key = key(theme)
        modeSupportCache.get(key)?.let { return it }
        val lightReadable = isReadable(scheme(theme, isDark = false))
        val darkReadable = isReadable(scheme(theme, isDark = true))
        return when {
            lightReadable && darkReadable -> ThemeModeSupport.BOTH
            darkReadable -> ThemeModeSupport.DARK_ONLY
            lightReadable -> ThemeModeSupport.LIGHT_ONLY
            else -> ThemeModeSupport.LOW_CONTRAST
        }.also { modeSupportCache.put(key, it) }
    }

    fun hasLowContrast(theme: CommunityTheme): Boolean {
        val key = key(theme)
        lowContrastCache.get(key)?.let { return it }
        val support = modeSupport(theme)
        val modes = when (support) {
            ThemeModeSupport.DARK_ONLY -> listOf(true)
            ThemeModeSupport.LIGHT_ONLY -> listOf(false)
            else -> listOf(false, true)
        }
        return (support == ThemeModeSupport.LOW_CONTRAST || modes.any { isDark ->
            TokenContrast.hasLowContrast(scheme(theme, isDark), TokenContrast.CORE_PAIRS)
        }).also { lowContrastCache.put(key, it) }
    }

    fun displayDark(theme: CommunityTheme, isDark: Boolean): Boolean = when (modeSupport(theme)) {
        ThemeModeSupport.DARK_ONLY -> true
        ThemeModeSupport.LIGHT_ONLY -> false
        else -> isDark
    }

    private fun isReadable(scheme: ResolvedScheme): Boolean =
        READABILITY_PAIRS.all { (foreground, background, minContrast) ->
            runCatching {
                ColorUtils.calculateContrast(
                    ColorUtils.setAlphaComponent(scheme[foreground], 255),
                    ColorUtils.setAlphaComponent(scheme[background], 255)
                ) >= minContrast
            }.getOrDefault(true)
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
