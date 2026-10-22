package com.drdisagree.colorblendr.theme.palette

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.materialcolorutilities.palettes.TonalPalette

class AdjustedPalette(
    val role: PaletteRole,
    private val palette: TonalPalette,
    private val tuning: PaletteTuning = PaletteTuning(),
    private val shadeOverrides: Map<Int, Int> = emptyMap()
) {
    val hue: Double
        get() = palette.hue

    val chroma: Double
        get() = palette.chroma

    val shades: List<Int> by lazy { ShadeScale.tones.map { colorAt(it) } }

    private val corrections: Map<Int, DoubleArray> by lazy {
        shadeOverrides.mapValues { (index, color) ->
            lab(color) - lab(tunedColor(ShadeScale.tones[index], 1.0))
        }
    }

    @ColorInt
    fun colorAt(tone: Double, chromaMultiplier: Double = 1.0): Int {
        shadeOverrides[ShadeScale.indexOfTone(tone)]?.let { return it }
        val color = tunedColor(tone, chromaMultiplier)
        val correction = correctionAt(tone) ?: return color
        val shifted = lab(color) + correction
        return ColorUtils.LABToColor(shifted[0].coerceIn(0.0, 100.0), shifted[1], shifted[2])
    }

    private fun tunedColor(tone: Double, chromaMultiplier: Double): Int {
        if (tuning.isIdentity && chromaMultiplier == 1.0) return palette.getHct(tone).toInt()
        return GamutClipper.scaled(
            palette.hue,
            palette.chroma * chromaMultiplier,
            tuning.tone(tone),
            tuning.chromaFactor
        )
    }

    private fun correctionAt(tone: Double): DoubleArray? {
        if (shadeOverrides.isEmpty()) return null

        val tones = ShadeScale.tones
        val upper = tones.indexOfLast { it > tone }.coerceIn(0, tones.lastIndex - 1)
        val lower = upper + 1
        val upperCorrection = corrections[upper]
        val lowerCorrection = corrections[lower]
        if (upperCorrection == null && lowerCorrection == null) return null

        val fraction = ((tones[upper] - tone) / (tones[upper] - tones[lower])).coerceIn(0.0, 1.0)
        val start = upperCorrection ?: ZERO
        val end = lowerCorrection ?: ZERO
        return DoubleArray(3) { start[it] + (end[it] - start[it]) * fraction }
    }

    private fun lab(color: Int): DoubleArray = DoubleArray(3).also { ColorUtils.colorToLAB(color, it) }

    private operator fun DoubleArray.plus(other: DoubleArray) = DoubleArray(3) { this[it] + other[it] }

    private operator fun DoubleArray.minus(other: DoubleArray) = DoubleArray(3) { this[it] - other[it] }

    private companion object {
        val ZERO = DoubleArray(3)
    }
}
