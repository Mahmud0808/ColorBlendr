package com.drdisagree.colorblendr.theme.model

import kotlin.math.abs

object ShadeScale {

    val shades: List<Int> = listOf(0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

    val tones: List<Double> =
        listOf(100.0, 99.0, 95.0, 90.0, 80.0, 70.0, 60.0, 50.0, 40.0, 30.0, 20.0, 10.0, 0.0)

    val indices: IntRange = shades.indices

    const val SHADE_900 = 11

    fun indexOfTone(tone: Double): Int = tones.indexOfFirst { abs(it - tone) < TONE_EPSILON }

    private const val TONE_EPSILON = 0.001
}
