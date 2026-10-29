package com.drdisagree.colorblendr.dev.utils

import androidx.annotation.ColorInt
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.sqrt

object PerceptualColor {

    private const val WHITE_X = 95.047
    private const val WHITE_Y = 100.0
    private const val WHITE_Z = 108.883
    private const val EPSILON = 216.0 / 24389.0
    private const val KAPPA = 24389.0 / 27.0

    fun lch(@ColorInt color: Int): FloatArray {
        val r = linearize((color shr 16 and 0xFF) / 255.0)
        val g = linearize((color shr 8 and 0xFF) / 255.0)
        val b = linearize((color and 0xFF) / 255.0)

        val x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) * 100.0
        val y = (0.2126729 * r + 0.7151522 * g + 0.0721750 * b) * 100.0
        val z = (0.0193339 * r + 0.1191920 * g + 0.9503041 * b) * 100.0

        val fx = pivot(x / WHITE_X)
        val fy = pivot(y / WHITE_Y)
        val fz = pivot(z / WHITE_Z)

        val lightness = 116.0 * fy - 16.0
        val a = 500.0 * (fx - fy)
        val bStar = 200.0 * (fy - fz)

        val chroma = sqrt(a * a + bStar * bStar)
        var hue = Math.toDegrees(atan2(bStar, a))
        if (hue < 0.0) hue += 360.0

        return floatArrayOf(lightness.toFloat(), chroma.toFloat(), hue.toFloat())
    }

    private fun linearize(channel: Double): Double =
        if (channel <= 0.04045) channel / 12.92 else ((channel + 0.055) / 1.055).pow(2.4)

    private fun pivot(value: Double): Double =
        if (value > EPSILON) cbrt(value) else (KAPPA * value + 16.0) / 116.0
}
