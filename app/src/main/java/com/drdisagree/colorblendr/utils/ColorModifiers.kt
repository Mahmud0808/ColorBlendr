package com.drdisagree.colorblendr.utils

import android.graphics.Color
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.utils.ColorUtil.adjustSaturation
import com.drdisagree.colorblendr.utils.ColorUtil.shiftLightness
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

object ColorModifiers {

    fun generateShades(hue: Float, chroma: Float): ArrayList<Int> {
        val shadeList = ArrayList(List(12) { 0 })

        shadeList[0] = ColorUtil.CAMToColor(
            hue,
            min(40.0, chroma.toDouble()).toFloat(), 99.0f
        )
        shadeList[1] = ColorUtil.CAMToColor(
            hue,
            min(40.0, chroma.toDouble()).toFloat(), 95.0f
        )

        for (i in 2..11) {
            val lstar = if (i == 6) {
                49.6f
            } else {
                (100 - ((i - 1) * 10)).toFloat()
            }
            shadeList[i] = ColorUtil.CAMToColor(hue, chroma, lstar)
        }

        return shadeList
    }

    fun modifyColors(
        palette: ArrayList<Int>,
        counter: AtomicInteger,
        style: MONET,
        monetAccentSaturation: Int,
        monetBackgroundSaturation: Int,
        monetBackgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean,
        modifyPitchBlack: Boolean,
        overrideColors: Boolean = true
    ): ArrayList<Int> {
        counter.getAndIncrement()

        val accentPalette = counter.get() <= 3

        val accentSaturation = monetAccentSaturation != 100
        val backgroundSaturation = monetBackgroundSaturation != 100
        val backgroundLightness = monetBackgroundLightness != 100

        val isMonochrome = style == MONET.MONOCHROMATIC
        val isRainbow = style == MONET.RAINBOW

        if (accentPalette) {
            if (accentSaturation && !isMonochrome) {
                // Set accent saturation
                palette.replaceAll { o: Int ->
                    adjustSaturation(
                        o,
                        monetAccentSaturation
                    )
                }
            }
        } else {
            if (backgroundLightness && !isMonochrome) {
                // Set background lightness
                for (j in palette.indices) {
                    palette[j] = shiftLightness(palette[j], monetBackgroundLightness, j + 1)
                }
            }

            if (backgroundSaturation && !isMonochrome && !isRainbow) {
                // Set background saturation
                palette.replaceAll { o: Int ->
                    adjustSaturation(
                        o,
                        monetBackgroundSaturation
                    )
                }
            }

            if (pitchBlackTheme && modifyPitchBlack) {
                // Set pitch black theme
                palette[10] = Color.BLACK
            }
        }

        if (isMonochrome) {
            // Set monochrome lightness
            for (j in palette.indices) {
                palette[j] = shiftLightness(palette[j], monetBackgroundLightness, j + 1)
            }
        }

        if (overrideColors) {
            for (j in 0 until palette.size - 1) {
                val i = counter.get() - 1

                val overriddenColor = Prefs.getInt(systemPaletteNames[i][j + 1], Int.MIN_VALUE)

                if (overriddenColor != Int.MIN_VALUE) {
                    palette[j] = overriddenColor
                } else if (!accurateShades && (i in 0..2) && j == 2) {
                    palette[j] = palette[j + 2]
                }
            }
        }

        if (counter.get() >= 5) {
            counter.set(0)
        }

        return palette
    }

    fun <K, V> zipToMap(keys: List<K>, values: List<V>): Map<K, V> {
        val result: MutableMap<K, V> = HashMap()

        require(keys.size == values.size) { "Lists must have the same size. Provided keys size: " + keys.size + " Provided values size: " + values.size + "." }

        for (i in keys.indices) {
            result[keys[i]] = values[i]
        }

        return result
    }
}
