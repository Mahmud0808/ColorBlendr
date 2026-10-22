package com.drdisagree.colorblendr.theme.palette

import com.drdisagree.materialcolorutilities.hct.Hct
import com.drdisagree.materialcolorutilities.palettes.TonalPalette
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

object GamutClipper {

    private const val CHROMA_EPSILON = 0.5
    private const val SEARCH_STEPS = 16
    private const val YELLOW_FIX_TONE = 99.0
    private const val ADAPTIVE_ALPHA = 1.0

    fun preserveTone(hue: Double, chroma: Double, tone: Double): Int =
        if (tone == YELLOW_FIX_TONE && Hct.isYellow(hue)) {
            TonalPalette.fromHueAndChroma(hue, chroma).tone(YELLOW_FIX_TONE.toInt())
        } else {
            Hct.from(hue, chroma, tone).toInt()
        }

    fun scaled(hue: Double, chroma: Double, tone: Double, factor: Double): Int {
        val target = Hct.from(hue, chroma, tone).chroma * factor
        return if (factor <= 1.0) {
            preserveTone(hue, target, tone)
        } else {
            adaptiveTowardsMid(hue, target, tone, ADAPTIVE_ALPHA)
        }
    }

    private fun adaptiveTowardsMid(hue: Double, chroma: Double, tone: Double, alpha: Double): Int {
        if (tone <= 0.0 || tone >= 100.0 || fits(hue, chroma, tone)) {
            return preserveTone(hue, chroma, tone)
        }

        val pivot = adaptiveMidTone(tone / 100.0, chroma / 100.0, alpha) * 100.0
        fun toneAt(c: Double) = pivot + (tone - pivot) * c / chroma

        var low = 0.0
        var high = chroma
        repeat(SEARCH_STEPS) {
            val mid = (low + high) / 2
            if (fits(hue, mid, toneAt(mid))) low = mid else high = mid
        }
        return Hct.from(hue, low, toneAt(low)).toInt()
    }

    private fun fits(hue: Double, chroma: Double, tone: Double): Boolean =
        Hct.from(hue, chroma, tone).chroma >= chroma - CHROMA_EPSILON

    private fun adaptiveMidTone(tone: Double, chroma: Double, alpha: Double): Double {
        val offset = tone - 0.5
        val e1 = 0.5 + abs(offset) + alpha * chroma
        return 0.5 * (1.0 + sign(offset) * (e1 - sqrt(e1 * e1 - 2.0 * abs(offset))))
    }
}
