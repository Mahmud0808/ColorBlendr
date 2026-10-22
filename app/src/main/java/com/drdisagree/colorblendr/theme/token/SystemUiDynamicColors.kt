package com.drdisagree.colorblendr.theme.token

import com.drdisagree.materialcolorutilities.dynamiccolor.ContrastCurve
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicColor
import com.drdisagree.materialcolorutilities.dynamiccolor.MaterialDynamicColors
import com.drdisagree.materialcolorutilities.dynamiccolor.ToneDeltaPair
import com.drdisagree.materialcolorutilities.dynamiccolor.ToneDeltaPair.DeltaConstraint
import com.drdisagree.materialcolorutilities.dynamiccolor.ToneDeltaPair.TonePolarity
import com.drdisagree.materialcolorutilities.dynamiccolor.Variant
import com.drdisagree.materialcolorutilities.hct.Hct
import com.drdisagree.materialcolorutilities.palettes.TonalPalette

class SystemUiDynamicColors(private val material: MaterialDynamicColors) {

    fun widgetBackground() = DynamicColor(
        name = "widget_background",
        palette = { it.secondaryPalette },
        tone = { if (it.isDark) 20.0 else 95.0 },
        isBackground = true
    )

    fun clockHour(): DynamicColor = DynamicColor(
        name = "clock_hour",
        palette = { if (it.isDark) it.primaryPalette else it.secondaryPalette },
        tone = { if (it.isDark) 80.0 else 30.0 },
        background = { widgetBackground() },
        contrastCurve = { ContrastCurve(4.0, 4.0, 5.0, 15.0) },
        toneDeltaPair = {
            ToneDeltaPair(clockHour(), clockMinute(), 10.0, TonePolarity.DARKER, constraint = DeltaConstraint.FARTHER)
        }
    )

    fun clockMinute() = DynamicColor(
        name = "clock_minute",
        palette = { it.primaryPalette },
        tone = { if (it.isDark) 90.0 else 40.0 },
        background = { widgetBackground() },
        contrastCurve = { ContrastCurve(6.5, 6.5, 10.0, 15.0) }
    )

    fun clockSecond() = DynamicColor(
        name = "clock_second",
        palette = { it.tertiaryPalette },
        tone = { if (it.isDark) 90.0 else 40.0 },
        background = { widgetBackground() },
        contrastCurve = { ContrastCurve(5.0, 5.0, 70.0, 11.0) }
    )

    fun weatherTemp() = DynamicColor(
        name = "weather_temp",
        palette = { it.primaryPalette },
        tone = { if (it.isDark) 80.0 else 40.0 },
        background = { widgetBackground() },
        contrastCurve = { ContrastCurve(5.0, 5.0, 70.0, 11.0) }
    )

    fun themeApp() = DynamicColor(
        name = "theme_app",
        palette = { if (it.isDark) it.secondaryPalette else it.primaryPalette },
        tone = { scheme ->
            val primary = scheme.primaryPalette
            if (scheme.isDark) {
                when (scheme.variant) {
                    Variant.TONAL_SPOT, Variant.EXPRESSIVE -> tMinC(primary, 20.0, 93.0)
                    Variant.VIBRANT -> tMinC(primary, 66.0, 93.0)
                    else -> 20.0
                }
            } else {
                when (scheme.variant) {
                    Variant.TONAL_SPOT -> tMaxC(primary, 0.0, 90.0)
                    Variant.EXPRESSIVE -> if (Hct.isCyan(primary.hue)) 88.0 else tMaxC(primary, 78.0, 90.0)
                    Variant.VIBRANT -> if (Hct.isCyan(primary.hue)) 88.0 else tMaxC(primary, 0.0, 66.0)
                    else -> 90.0
                }
            }
        },
        isBackground = true
    )

    fun onThemeApp() = DynamicColor(
        name = "on_theme_app",
        palette = { it.primaryPalette },
        tone = { if (it.isDark) 80.0 else 30.0 },
        background = { themeApp() },
        contrastCurve = { ContrastCurve(7.0, 7.0, 11.0, 21.0) }
    )

    fun themeAppRing() = DynamicColor(
        name = "theme_app_ring",
        palette = { it.primaryPalette },
        tone = { scheme ->
            when (scheme.variant) {
                Variant.TONAL_SPOT, Variant.EXPRESSIVE -> tMaxC(scheme.primaryPalette, 0.0, 70.0)
                Variant.VIBRANT -> tMaxC(scheme.primaryPalette, 0.0, 100.0)
                else -> 70.0
            }
        },
        isBackground = true,
        background = { material.surfaceContainerHigh },
        contrastCurve = { ContrastCurve(1.8, 1.8, 3.0, 4.5) }
    )

    fun themeNotif(): DynamicColor = DynamicColor(
        name = "theme_notif",
        palette = { it.tertiaryPalette },
        tone = { tMinC(it.tertiaryPalette, 80.0, 93.0) },
        background = { themeAppRing() },
        contrastCurve = { ContrastCurve(1.0, 1.0, 1.0, 1.0) },
        toneDeltaPair = {
            ToneDeltaPair(
                themeNotif(),
                themeAppRing(),
                5.0,
                TonePolarity.RELATIVE_LIGHTER,
                constraint = DeltaConstraint.FARTHER
            )
        }
    )

    fun brandA() = DynamicColor(
        name = "brand_a",
        palette = { it.primaryPalette },
        tone = { if (it.isDark) 80.0 else 40.0 },
        background = { material.surfaceContainerLow },
        contrastCurve = {
            if (it.isDark) ContrastCurve(10.0, 10.0, 12.0, 13.0) else ContrastCurve(6.0, 6.0, 9.0, 12.0)
        }
    )

    fun brandB() = DynamicColor(
        name = "brand_b",
        palette = { it.secondaryPalette },
        tone = { if (it.isDark) 98.0 else 70.0 },
        background = { material.surfaceContainerLow },
        contrastCurve = {
            if (it.isDark) ContrastCurve(16.0, 16.0, 16.5, 17.0) else ContrastCurve(2.0, 2.0, 3.0, 4.5)
        }
    )

    fun brandC() = DynamicColor(
        name = "brand_c",
        palette = { it.primaryPalette },
        tone = { if (it.isDark) 60.0 else 50.0 },
        background = { material.surfaceContainerLow },
        contrastCurve = {
            if (it.isDark) ContrastCurve(6.0, 6.0, 9.0, 11.0) else ContrastCurve(4.0, 4.0, 7.0, 8.0)
        }
    )

    fun brandD() = DynamicColor(
        name = "brand_d",
        palette = { it.tertiaryPalette },
        tone = { if (it.isDark) 90.0 else 59.0 },
        background = { material.surfaceContainerLow },
        contrastCurve = {
            if (it.isDark) ContrastCurve(13.0, 13.0, 14.0, 15.0) else ContrastCurve(3.0, 3.0, 4.5, 6.0)
        }
    )

    fun underSurface() = DynamicColor(
        name = "under_surface",
        palette = { it.primaryPalette },
        tone = { 0.0 },
        isBackground = true
    )

    fun shadeActive(): DynamicColor = DynamicColor(
        name = "shade_active",
        palette = { it.primaryPalette },
        tone = { 90.0 },
        isBackground = true,
        background = { underSurface() },
        contrastCurve = { ContrastCurve(3.0, 3.0, 4.5, 7.0) },
        toneDeltaPair = {
            ToneDeltaPair(shadeActive(), shadeInactive(), 30.0, TonePolarity.LIGHTER, constraint = DeltaConstraint.FARTHER)
        }
    )

    fun onShadeActive(): DynamicColor = DynamicColor(
        name = "on_shade_active",
        palette = { it.primaryPalette },
        tone = { 10.0 },
        background = { shadeActive() },
        contrastCurve = { ContrastCurve(4.5, 4.5, 7.0, 11.0) },
        toneDeltaPair = {
            ToneDeltaPair(
                onShadeActive(),
                onShadeActiveVariant(),
                20.0,
                TonePolarity.RELATIVE_LIGHTER,
                constraint = DeltaConstraint.FARTHER
            )
        }
    )

    fun onShadeActiveVariant() = DynamicColor(
        name = "on_shade_active_variant",
        palette = { it.primaryPalette },
        tone = { 30.0 },
        background = { shadeActive() },
        contrastCurve = { ContrastCurve(4.5, 4.5, 7.0, 11.0) }
    )

    fun shadeInactive(): DynamicColor = DynamicColor(
        name = "shade_inactive",
        palette = { it.neutralPalette },
        tone = { 20.0 },
        isBackground = true,
        background = { underSurface() },
        contrastCurve = { ContrastCurve(1.0, 1.0, 1.0, 1.0) },
        toneDeltaPair = {
            ToneDeltaPair(shadeInactive(), shadeDisabled(), 15.0, TonePolarity.LIGHTER, constraint = DeltaConstraint.FARTHER)
        }
    )

    fun onShadeInactive(): DynamicColor = DynamicColor(
        name = "on_shade_inactive",
        palette = { it.neutralVariantPalette },
        tone = { 90.0 },
        background = { shadeInactive() },
        contrastCurve = { ContrastCurve(4.5, 4.5, 7.0, 11.0) },
        toneDeltaPair = {
            ToneDeltaPair(
                onShadeInactive(),
                onShadeInactiveVariant(),
                10.0,
                TonePolarity.RELATIVE_LIGHTER,
                constraint = DeltaConstraint.FARTHER
            )
        }
    )

    fun onShadeInactiveVariant() = DynamicColor(
        name = "on_shade_inactive_variant",
        palette = { it.neutralVariantPalette },
        tone = { 80.0 },
        background = { shadeInactive() },
        contrastCurve = { ContrastCurve(4.5, 4.5, 7.0, 11.0) }
    )

    fun shadeDisabled() = DynamicColor(
        name = "shade_disabled",
        palette = { it.neutralPalette },
        tone = { 4.0 },
        background = { underSurface() },
        contrastCurve = { ContrastCurve(1.0, 1.0, 1.0, 1.0) }
    )

    fun overviewBackground() = DynamicColor(
        name = "overview_background",
        palette = { it.neutralVariantPalette },
        tone = { if (it.isDark) 35.0 else 80.0 },
        isBackground = true
    )

    private fun tMaxC(palette: TonalPalette, lowerBound: Double, upperBound: Double): Double =
        findBestToneForChroma(palette.hue, palette.chroma, 100.0, byDecreasingTone = true)
            .coerceIn(lowerBound, upperBound)

    private fun tMinC(palette: TonalPalette, lowerBound: Double, upperBound: Double): Double =
        findBestToneForChroma(palette.hue, palette.chroma, 0.0, byDecreasingTone = false)
            .coerceIn(lowerBound, upperBound)

    private fun findBestToneForChroma(
        hue: Double,
        chroma: Double,
        startTone: Double,
        byDecreasingTone: Boolean
    ): Double {
        var tone = startTone
        var answer = tone
        var bestCandidate = Hct.from(hue, chroma, answer)
        while (bestCandidate.chroma < chroma) {
            if (tone < 0 || tone > 100) break
            tone += if (byDecreasingTone) -1.0 else 1.0
            val candidate = Hct.from(hue, chroma, tone)
            if (bestCandidate.chroma < candidate.chroma) {
                bestCandidate = candidate
                answer = tone
            }
        }
        return answer
    }
}
