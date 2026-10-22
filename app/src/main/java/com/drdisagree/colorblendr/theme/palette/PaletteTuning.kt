package com.drdisagree.colorblendr.theme.palette

import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments.Companion.NEUTRAL_LEVEL
import com.drdisagree.materialcolorutilities.hct.Hct

class PaletteTuning(
    private val saturation: Int = NEUTRAL_LEVEL,
    private val lightness: Int = NEUTRAL_LEVEL,
    val chromaBoost: Double = 1.0
) {
    val isIdentity: Boolean
        get() = saturation == NEUTRAL_LEVEL && lightness == NEUTRAL_LEVEL && chromaBoost == 1.0

    fun tone(tone: Double): Double {
        if (lightness == NEUTRAL_LEVEL) return tone
        val shift = (lightness - NEUTRAL_LEVEL) / LIGHTNESS_STEPS_PER_TONE * damping(tone)
        return (tone + shift).coerceIn(0.0, 100.0)
    }

    fun chroma(hue: Double, chroma: Double, tone: Double): Double {
        if (saturation == NEUTRAL_LEVEL) return chroma
        val amount = (saturation - NEUTRAL_LEVEL) / NEUTRAL_LEVEL.toDouble()
        val available = Hct.from(hue, chroma, tone).chroma
        return if (amount > 0) {
            available + (Hct.from(hue, MAX_CHROMA, tone).chroma - available) * amount
        } else {
            available * (1 + amount)
        }
    }

    private fun damping(tone: Double): Double {
        val upper = DAMPING.indexOfFirst { it.first >= tone }
        if (upper <= 0) return DAMPING.first().second
        val (toneA, valueA) = DAMPING[upper - 1]
        val (toneB, valueB) = DAMPING[upper]
        return valueA + (valueB - valueA) * (tone - toneA) / (toneB - toneA)
    }

    companion object {
        private const val MAX_CHROMA = 200.0
        private const val LIGHTNESS_STEPS_PER_TONE = 10.0
        private const val ACCENT_VIBRANCE = 1.2

        private val DAMPING = listOf(
            0.0 to 0.0,
            10.0 to 1.0,
            90.0 to 1.0,
            95.0 to 0.5,
            99.0 to 0.1,
            100.0 to 0.0
        )

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
            val chromaBoost = if (role.isAccent && !adjustments.accurateShades) {
                ACCENT_VIBRANCE
            } else {
                1.0
            }
            return PaletteTuning(saturation, lightness, chromaBoost)
        }
    }
}
