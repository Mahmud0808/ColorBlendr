package com.drdisagree.colorblendr.theme

import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.palette.AdjustedPalette
import com.drdisagree.colorblendr.theme.palette.PaletteTuning
import com.drdisagree.colorblendr.theme.rule.PitchBlackRule
import com.drdisagree.colorblendr.theme.rule.ThemeRule
import com.drdisagree.colorblendr.theme.rule.TintlessTextRule
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.scheme.SchemeFactory
import com.drdisagree.colorblendr.theme.scheme.SchemeFactory.palette
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicScheme
import com.drdisagree.materialcolorutilities.dynamiccolor.MaterialDynamicColors

object ThemeEngine {

    fun resolve(
        source: ThemeSource,
        adjustments: ThemeAdjustments,
        lightAdjustments: ThemeAdjustments = adjustments
    ): ResolvedTheme = ResolvedTheme(
        light = scheme(source, lightAdjustments, isDark = false),
        dark = scheme(source, adjustments, isDark = true),
        adjustments = adjustments
    )

    fun scheme(source: ThemeSource, adjustments: ThemeAdjustments, isDark: Boolean): ResolvedScheme {
        val dynamicScheme = SchemeFactory.create(source, isDark)
        val palettes = PaletteRole.entries.associateWith { role ->
            AdjustedPalette(
                role = role,
                palette = dynamicScheme.palette(role),
                tuning = PaletteTuning.forRole(role, source.style, adjustments),
                shadeOverrides = adjustments.shadeOverridesFor(role)
            )
        }
        return ResolvedScheme(isDark, dynamicScheme, palettes, rules(adjustments, dynamicScheme))
    }

    private fun rules(adjustments: ThemeAdjustments, scheme: DynamicScheme): List<ThemeRule> = buildList {
        if (adjustments.pitchBlack) {
            add(
                PitchBlackRule(
                    surfaceTone = MaterialDynamicColors().surface.getTone(scheme),
                    blackContainers = adjustments.pitchBlackContainers
                )
            )
        }
        if (!adjustments.tintedText) add(TintlessTextRule)
    }
}
