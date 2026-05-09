package com.drdisagree.colorblendr.utils.colors

import android.content.Context
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.utils.app.MiscUtil.getOriginalString
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.materialcolorutilities.dynamiccolor.ColorSpec
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

object ColorSchemeUtil {

    private val tones: IntArray = intArrayOf(100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0)

    fun generateColorPalette(
        style: MONET,
        @ColorInt color: Int,
        isDark: Boolean = SystemUtil.isDarkMode,
        contrast: Int = 0
    ): ArrayList<ArrayList<Int>> {
        val palette = ArrayList<ArrayList<Int>>()
        val specVersion: ColorSpec.SpecVersion = when (Utilities.getColorSpecVersion()) {
            0 -> ColorSpec.SpecVersion.SPEC_2021
            1 -> ColorSpec.SpecVersion.SPEC_2025
            2 -> ColorSpec.SpecVersion.SPEC_2026
            else -> ColorSpec.SpecVersion.SPEC_2021
        }
        val platform: DynamicScheme.Platform = DynamicScheme.DEFAULT_PLATFORM

        val dynamicScheme = getDynamicScheme(
            style,
            color,
            isDark,
            contrast,
            specVersion,
            platform
        )

        val tonalPalettes = arrayOf(
            dynamicScheme.primaryPalette,
            dynamicScheme.secondaryPalette,
            dynamicScheme.tertiaryPalette,
            dynamicScheme.neutralPalette,
            dynamicScheme.neutralVariantPalette,
            dynamicScheme.errorPalette
        )

        for (tonalPalette in tonalPalettes) {
            palette.add(createToneList(tonalPalette))
        }

        return palette
    }

    private fun getDynamicScheme(
        style: MONET,
        @ColorInt color: Int,
        isDark: Boolean,
        contrast: Int,
        specVersion: ColorSpec.SpecVersion,
        platform: DynamicScheme.Platform
    ): DynamicScheme {
        return when (style) {
            MONET.SPRITZ -> SchemeNeutral(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.MONOCHROMATIC -> SchemeMonochrome(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.TONAL_SPOT -> SchemeTonalSpot(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.VIBRANT -> SchemeVibrant(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.RAINBOW -> SchemeRainbow(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.EXPRESSIVE -> SchemeExpressive(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.FIDELITY -> SchemeFidelity(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.CONTENT -> SchemeContent(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.FRUIT_SALAD -> SchemeFruitSalad(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )

            MONET.CMF -> SchemeCmf(
                sourceColorHct = Hct.fromInt(color),
                isDark = isDark,
                contrastLevel = contrast.toDouble(),
                specVersion = specVersion,
                platform = platform
            )
        }
    }

    private fun createToneList(palette: TonalPalette): ArrayList<Int> {
        val toneList = ArrayList<Int>()
        for (tone in tones) {
            toneList.add(palette.tone(tone))
        }
        return toneList
    }

    fun Int.getStyleNameForRootless(): String {
        return when (this.getOriginalString()) {
            R.string.monet_neutral.getOriginalString() -> "SPRITZ"
            R.string.monet_vibrant.getOriginalString() -> "VIBRANT"
            R.string.monet_expressive.getOriginalString() -> "EXPRESSIVE"
            R.string.monet_rainbow.getOriginalString() -> "RAINBOW"
            R.string.monet_fruitsalad.getOriginalString() -> "FRUIT_SALAD"
            R.string.monet_content.getOriginalString() -> "CONTENT"
            R.string.monet_monochrome.getOriginalString() -> "MONOCHROMATIC"
            R.string.monet_fidelity.getOriginalString() -> "FIDELITY"
            else -> "TONAL_SPOT"
        }
    }

    fun stringToEnumMonetStyle(
        context: Context = appContext,
        enumString: String
    ): MONET {
        // compare both original string and localized string
        return when (enumString) {
            R.string.monet_neutral.getOriginalString(),
            context.getString(R.string.monet_neutral) -> MONET.SPRITZ

            R.string.monet_monochrome.getOriginalString(),
            context.getString(R.string.monet_monochrome) -> MONET.MONOCHROMATIC

            R.string.monet_tonalspot.getOriginalString(),
            context.getString(R.string.monet_tonalspot) -> MONET.TONAL_SPOT

            R.string.monet_vibrant.getOriginalString(),
            context.getString(R.string.monet_vibrant) -> MONET.VIBRANT

            R.string.monet_rainbow.getOriginalString(),
            context.getString(R.string.monet_rainbow) -> MONET.RAINBOW

            R.string.monet_expressive.getOriginalString(),
            context.getString(R.string.monet_expressive) -> MONET.EXPRESSIVE

            R.string.monet_fidelity.getOriginalString(),
            context.getString(R.string.monet_fidelity) -> MONET.FIDELITY

            R.string.monet_content.getOriginalString(),
            context.getString(R.string.monet_content) -> MONET.CONTENT

            R.string.monet_fruitsalad.getOriginalString(),
            context.getString(R.string.monet_fruitsalad) -> MONET.FRUIT_SALAD

            else -> MONET.TONAL_SPOT
        }
    }
}