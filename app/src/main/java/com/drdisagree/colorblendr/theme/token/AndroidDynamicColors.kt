package com.drdisagree.colorblendr.theme.token

import com.drdisagree.materialcolorutilities.dynamiccolor.ColorSpec.SpecVersion
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicColor
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicScheme
import com.drdisagree.materialcolorutilities.dynamiccolor.MaterialDynamicColors
import com.drdisagree.materialcolorutilities.dynamiccolor.extendSpecVersion
import com.drdisagree.materialcolorutilities.palettes.TonalPalette

class AndroidDynamicColors(private val material: MaterialDynamicColors) {

    val controlActivated: DynamicColor
        get() = fixedTone("control_activated", { it.primaryPalette }, dark = 30.0, light = 90.0)
            .since2025(material.primaryContainer)

    val controlNormal: DynamicColor
        get() = fixedTone("control_normal", { it.neutralVariantPalette }, dark = 80.0, light = 30.0)
            .since2025(material.onSurfaceVariant)

    val controlHighlight: DynamicColor
        get() = DynamicColor(
            name = "control_highlight",
            palette = { it.neutralPalette },
            tone = { if (it.isDark) 100.0 else 0.0 },
            opacity = { if (it.isDark) 0.20 else 0.12 }
        )

    val textPrimaryInverse: DynamicColor
        get() = fixedTone("text_primary_inverse", { it.neutralPalette }, dark = 10.0, light = 90.0)
            .since2025(material.inverseOnSurface)

    val textSecondaryAndTertiaryInverse: DynamicColor
        get() = fixedTone(
            "text_secondary_and_tertiary_inverse",
            { it.neutralVariantPalette },
            dark = 30.0,
            light = 80.0
        )

    val textPrimaryInverseDisableOnly: DynamicColor
        get() = fixedTone(
            "text_primary_inverse_disable_only",
            { it.neutralPalette },
            dark = 10.0,
            light = 90.0
        )

    val textSecondaryAndTertiaryInverseDisabled: DynamicColor
        get() = fixedTone(
            "text_secondary_and_tertiary_inverse_disabled",
            { it.neutralPalette },
            dark = 10.0,
            light = 90.0
        )

    val textHintInverse: DynamicColor
        get() = fixedTone("text_hint_inverse", { it.neutralPalette }, dark = 10.0, light = 90.0)

    val genWidget: DynamicColor
        get() = fixedTone("gen_widget", { it.primaryPalette }, dark = 45.0, light = 75.0)

    private fun DynamicColor.since2025(replacement: DynamicColor): DynamicColor =
        extendSpecVersion(
            SpecVersion.SPEC_2025,
            replacement.copy(name = name, isBackground = isBackground)
        )

    private fun fixedTone(
        name: String,
        palette: (DynamicScheme) -> TonalPalette,
        dark: Double,
        light: Double
    ): DynamicColor = DynamicColor(
        name = name,
        palette = palette,
        tone = { if (it.isDark) dark else light }
    )
}
