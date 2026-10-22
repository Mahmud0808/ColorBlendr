package com.drdisagree.colorblendr.theme.palette

import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments.Companion.NEUTRAL_LEVEL
import kotlin.math.abs

class PaletteTuning(
    private val saturation: Int = NEUTRAL_LEVEL,
    private val lightness: Int = NEUTRAL_LEVEL,
    accurateShades: Boolean = true
) {
    val vivid: Boolean = !accurateShades

    val chromaFactor: Double =
        saturation / NEUTRAL_LEVEL.toDouble() * (if (vivid) VIVID_SHADES_FACTOR else 1.0)

    val isIdentity: Boolean
        get() = lightness == NEUTRAL_LEVEL && chromaFactor == 1.0 && !vivid

    private val toneBlend: Double = (NEUTRAL_LEVEL - lightness) / NEUTRAL_LEVEL.toDouble()

    fun tone(tone: Double): Double {
        val shifted = lightnessTone(tone)
        return if (vivid) vividTone(shifted) else shifted
    }

    private fun lightnessTone(tone: Double): Double {
        if (lightness == NEUTRAL_LEVEL) return tone
        val clamped = tone.coerceIn(0.0, 100.0)
        val curve = if (toneBlend > 0) DARKEST_CURVE else BRIGHTEST_CURVE
        return clamped + abs(toneBlend) * (curve.map(clamped) - clamped)
    }

    private fun vividTone(tone: Double): Double {
        if (tone <= MID_TONE) return tone
        val fromWhite = ((100.0 - tone) / VIVID_TAPER_TONES).coerceIn(0.0, 1.0)
        val fromMid = ((tone - MID_TONE) / VIVID_TAPER_TONES).coerceIn(0.0, 1.0)
        return tone - VIVID_TONE_SHIFT * fromWhite * fromMid
    }

    private fun List<Pair<Double, Double>>.map(tone: Double): Double {
        val upper = indexOfFirst { it.first >= tone }.coerceAtLeast(1)
        val (fromA, toA) = this[upper - 1]
        val (fromB, toB) = this[upper]
        return toA + (toB - toA) * (tone - fromA) / (fromB - fromA)
    }

    companion object {
        private val DARKEST_CURVE = listOf(0.0 to 0.0, 6.0 to 0.0, 12.0 to 2.0, 90.0 to 80.0, 100.0 to 100.0)
        private val BRIGHTEST_CURVE = listOf(0.0 to 0.0, 10.0 to 20.0, 88.0 to 98.0, 100.0 to 100.0)
        private const val VIVID_SHADES_FACTOR = 1.25
        private const val VIVID_TONE_SHIFT = 6.0
        private const val VIVID_TAPER_TONES = 10.0
        private const val MID_TONE = 50.0

        fun forRole(role: PaletteRole, style: MONET, adjustments: ThemeAdjustments): PaletteTuning {
            val monochrome = style == MONET.MONOCHROMATIC
            val saturation = when {
                monochrome -> NEUTRAL_LEVEL
                role.isNeutral && style == MONET.RAINBOW -> NEUTRAL_LEVEL
                role.isNeutral -> adjustments.backgroundSaturation
                else -> adjustments.accentSaturation
            }
            val lightness = if (monochrome || role.isNeutral) {
                adjustments.backgroundLightness
            } else {
                NEUTRAL_LEVEL
            }
            return PaletteTuning(
                saturation = saturation,
                lightness = lightness,
                accurateShades = adjustments.accurateShades || monochrome || !role.isAccent
            )
        }
    }
}
