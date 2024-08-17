package com.drdisagree.colorblendr.utils

import android.content.Context
import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.utils.monet.dynamiccolor.DynamicScheme
import com.drdisagree.colorblendr.utils.monet.hct.Hct
import com.drdisagree.colorblendr.utils.monet.palettes.TonalPalette
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeContent
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeExpressive
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeFidelity
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeFruitSalad
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeMonochrome
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeNeutral
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeRainbow
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeTonalSpot
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeVibrant

object ColorSchemeUtil {
    private val tones: IntArray = intArrayOf(100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0)

    fun generateColorPalette(
        style: MONET,
        @ColorInt color: Int,
        isDark: Boolean = SystemUtil.isDarkMode,
        contrast: Int = 5
    ): ArrayList<ArrayList<Int>> {
        val palette = ArrayList<ArrayList<Int>>()

        val dynamicScheme = getDynamicScheme(style, color, isDark, contrast)

        val tonalPalettes = arrayOf(
            dynamicScheme.primaryPalette,
            dynamicScheme.secondaryPalette,
            dynamicScheme.tertiaryPalette,
            dynamicScheme.neutralPalette,
            dynamicScheme.neutralVariantPalette
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
        contrast: Int
    ): DynamicScheme {
        return when (style) {
            MONET.SPRITZ -> SchemeNeutral(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.MONOCHROMATIC -> SchemeMonochrome(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.TONAL_SPOT -> SchemeTonalSpot(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.VIBRANT -> SchemeVibrant(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.RAINBOW -> SchemeRainbow(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.EXPRESSIVE -> SchemeExpressive(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.FIDELITY -> SchemeFidelity(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.CONTENT -> SchemeContent(Hct.fromInt(color), isDark, contrast.toDouble())
            MONET.FRUIT_SALAD -> SchemeFruitSalad(Hct.fromInt(color), isDark, contrast.toDouble())
        }
    }

    private fun createToneList(palette: TonalPalette): ArrayList<Int> {
        val toneList = ArrayList<Int>()
        for (tone in tones) {
            toneList.add(palette.tone(tone))
        }
        return toneList
    }

    fun stringToEnumMonetStyle(context: Context, enumString: String): MONET {
        return when (enumString) {
            context.getString(R.string.monet_neutral) -> {
                MONET.SPRITZ
            }

            context.getString(R.string.monet_monochrome) -> {
                MONET.MONOCHROMATIC
            }

            context.getString(R.string.monet_tonalspot) -> {
                MONET.TONAL_SPOT
            }

            context.getString(R.string.monet_vibrant) -> {
                MONET.VIBRANT
            }

            context.getString(R.string.monet_rainbow) -> {
                MONET.RAINBOW
            }

            context.getString(R.string.monet_expressive) -> {
                MONET.EXPRESSIVE
            }

            context.getString(R.string.monet_fidelity) -> {
                MONET.FIDELITY
            }

            context.getString(R.string.monet_content) -> {
                MONET.CONTENT
            }

            context.getString(R.string.monet_fruitsalad) -> {
                MONET.FRUIT_SALAD
            }

            else -> {
                MONET.TONAL_SPOT
            }
        }
    }

    enum class MONET {
        SPRITZ,
        MONOCHROMATIC,
        TONAL_SPOT,
        VIBRANT,
        RAINBOW,
        EXPRESSIVE,
        FIDELITY,
        CONTENT,
        FRUIT_SALAD
    }
}
