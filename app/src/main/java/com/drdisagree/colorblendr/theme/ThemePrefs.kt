package com.drdisagree.colorblendr.theme

import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.forcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getColorSpecVersion
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.getSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getTokenOverrides
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.legacyTokensEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.secondaryColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tertiaryColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.TokenOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.utils.app.SystemUtil

object ThemePrefs {

    fun source(
        style: MONET = getCurrentMonetStyle(),
        @ColorInt seedColor: Int = currentSeedColor()
    ): ThemeSource = ThemeSource(
        style = style,
        seedColor = seedColor,
        secondaryColor = if (secondaryColorEnabled()) getSecondaryColorValue() else null,
        tertiaryColor = if (tertiaryColorEnabled()) getTertiaryColorValue() else null,
        specVersion = getColorSpecVersion()
    )

    fun adjustments(
        isDark: Boolean = SystemUtil.isDarkMode,
        accentSaturation: Int = getAccentSaturation(isDark),
        backgroundSaturation: Int = getBackgroundSaturation(isDark),
        backgroundLightness: Int = getBackgroundLightness(isDark),
        includeOverrides: Boolean = true
    ): ThemeAdjustments = ThemeAdjustments(
        accentSaturation = accentSaturation,
        backgroundSaturation = backgroundSaturation,
        backgroundLightness = backgroundLightness,
        accurateShades = accurateShadesEnabled(),
        pitchBlack = pitchBlackThemeEnabled(),
        pitchBlackContainers = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
                forcePitchBlackSettingsEnabled(),
        tintedText = tintedTextEnabled(),
        shadeOverrides = if (includeOverrides) shadeOverrides() else emptyMap(),
        tokenOverrides = if (includeOverrides) getTokenOverrides() else TokenOverrides(),
        legacyTokens = includeOverrides && legacyTokensEnabled()
    )

    fun resolve(): ResolvedTheme = ThemeEngine.resolve(
        source = source(),
        adjustments = adjustments(isDark = true),
        lightAdjustments = adjustments(isDark = false)
    )

    fun scheme(
        isDark: Boolean = SystemUtil.isDarkMode,
        source: ThemeSource = source(),
        adjustments: ThemeAdjustments = adjustments(isDark)
    ): ResolvedScheme = ThemeEngine.scheme(source, adjustments, isDark)

    @ColorInt
    private fun currentSeedColor(): Int =
        getSeedColorValue(getWallpaperColorList().firstOrNull() ?: Color.BLUE)

    private fun shadeOverrides(): Map<String, Int> = buildMap {
        PaletteRole.shadeNames.flatten().forEach { name ->
            val color = Prefs.getInt(name, Int.MIN_VALUE)
            if (color != Int.MIN_VALUE) put(name, color)
        }
    }
}
