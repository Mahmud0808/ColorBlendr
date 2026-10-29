package com.drdisagree.colorblendr.theme.scheme

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.materialcolorutilities.dynamiccolor.ColorSpec.SpecVersion
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicScheme
import com.drdisagree.materialcolorutilities.hct.Hct
import com.drdisagree.materialcolorutilities.palettes.TonalPalette
import com.drdisagree.materialcolorutilities.scheme.SchemeCmf
import com.drdisagree.materialcolorutilities.scheme.SchemeContent
import com.drdisagree.materialcolorutilities.scheme.SchemeExpressive
import com.drdisagree.materialcolorutilities.scheme.SchemeFidelity
import com.drdisagree.materialcolorutilities.scheme.SchemeFruitSalad
import com.drdisagree.materialcolorutilities.scheme.SchemeMonochrome
import com.drdisagree.materialcolorutilities.scheme.SchemeNeutral
import com.drdisagree.materialcolorutilities.scheme.SchemeRainbow
import com.drdisagree.materialcolorutilities.scheme.SchemeTonalSpot
import com.drdisagree.materialcolorutilities.scheme.SchemeVibrant

object SchemeFactory {

    fun create(source: ThemeSource, isDark: Boolean): DynamicScheme {
        val base = create(source.style, source.seedColor, isDark, source.contrast, source.specVersion)
        if (source.secondaryColor == null && source.tertiaryColor == null) return base

        return base.withPalettes(
            secondary = source.secondaryColor?.let { accentPalette(source, it, isDark) },
            tertiary = source.tertiaryColor?.let { accentPalette(source, it, isDark) }
        )
    }

    fun DynamicScheme.palette(role: PaletteRole): TonalPalette = when (role) {
        PaletteRole.PRIMARY -> primaryPalette
        PaletteRole.SECONDARY -> secondaryPalette
        PaletteRole.TERTIARY -> tertiaryPalette
        PaletteRole.NEUTRAL -> neutralPalette
        PaletteRole.NEUTRAL_VARIANT -> neutralVariantPalette
        PaletteRole.ERROR -> errorPalette
    }

    fun DynamicScheme.roleOf(palette: TonalPalette): PaletteRole? =
        PaletteRole.entries.firstOrNull { palette(it) === palette }

    fun specVersion(version: Int): SpecVersion = when (version) {
        1 -> SpecVersion.SPEC_2025
        2 -> SpecVersion.SPEC_2026
        else -> SpecVersion.SPEC_2021
    }

    private fun accentPalette(source: ThemeSource, @ColorInt color: Int, isDark: Boolean): TonalPalette =
        create(source.style, color, isDark, source.contrast, source.specVersion).primaryPalette

    private fun DynamicScheme.withPalettes(
        secondary: TonalPalette?,
        tertiary: TonalPalette?
    ): DynamicScheme = DynamicScheme(
        sourceColorHctList = sourceColorHctList,
        variant = variant,
        isDark = isDark,
        contrastLevel = contrastLevel,
        platform = platform,
        specVersion = specVersion,
        primaryPalette = primaryPalette,
        secondaryPalette = secondary ?: secondaryPalette,
        tertiaryPalette = tertiary ?: tertiaryPalette,
        neutralPalette = neutralPalette,
        neutralVariantPalette = neutralVariantPalette,
        errorPalette = errorPalette
    )

    private fun create(
        style: MONET,
        @ColorInt seedColor: Int,
        isDark: Boolean,
        contrast: Double,
        specVersion: Int
    ): DynamicScheme {
        val sourceColorHct = Hct.fromInt(seedColor)
        val spec = specVersion(specVersion)
        val platform = DynamicScheme.DEFAULT_PLATFORM

        return when (style) {
            MONET.SPRITZ -> SchemeNeutral(sourceColorHct, isDark, contrast, spec, platform)
            MONET.MONOCHROMATIC -> SchemeMonochrome(sourceColorHct, isDark, contrast, spec, platform)
            MONET.TONAL_SPOT -> SchemeTonalSpot(sourceColorHct, isDark, contrast, spec, platform)
            MONET.VIBRANT -> SchemeVibrant(sourceColorHct, isDark, contrast, spec, platform)
            MONET.RAINBOW -> SchemeRainbow(sourceColorHct, isDark, contrast, spec, platform)
            MONET.EXPRESSIVE -> SchemeExpressive(sourceColorHct, isDark, contrast, spec, platform)
            MONET.FIDELITY -> SchemeFidelity(sourceColorHct, isDark, contrast, spec, platform)
            MONET.CONTENT -> SchemeContent(sourceColorHct, isDark, contrast, spec, platform)
            MONET.FRUIT_SALAD -> SchemeFruitSalad(sourceColorHct, isDark, contrast, spec, platform)
            MONET.CMF -> SchemeCmf(sourceColorHct, isDark, contrast, SpecVersion.SPEC_2026, platform)
        }
    }
}
