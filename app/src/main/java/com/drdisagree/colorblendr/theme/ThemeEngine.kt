package com.drdisagree.colorblendr.theme

import com.drdisagree.colorblendr.theme.legacy.LegacyTokenRule
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
import com.drdisagree.colorblendr.theme.token.TokenTones
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
        return ResolvedScheme(
            isDark = isDark,
            palettes = palettes,
            rules = rules(adjustments, dynamicScheme, palettes),
            overrides = adjustments.tokenOverrides.forMode(isDark),
            tones = { TokenTones.of(source, dynamicScheme) }
        )
    }

    private fun rules(
        adjustments: ThemeAdjustments,
        scheme: DynamicScheme,
        palettes: Map<PaletteRole, AdjustedPalette>
    ): List<ThemeRule> = buildList {
        if (adjustments.legacyTokens) {
            val roles = LegacyTokenRule.rolesOf(adjustments.shadeOverrides)
            if (roles.isNotEmpty()) add(LegacyTokenRule(roles, palettes, adjustments.pitchBlack))
        }
        if (adjustments.pitchBlack) {
            add(PitchBlackRule(surfaceTone = MaterialDynamicColors().surface.getTone(scheme)))
        }
        if (!adjustments.tintedText) add(TintlessTextRule)
    }
}
