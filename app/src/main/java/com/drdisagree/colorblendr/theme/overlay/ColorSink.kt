package com.drdisagree.colorblendr.theme.overlay

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

fun interface ColorSink {

    fun put(name: String, @ColorInt color: Int, night: Boolean, ifExists: Boolean)

    fun color(name: String, @ColorInt color: Int, ifExists: Boolean = true) =
        put(name, color, night = false, ifExists = ifExists)

    fun night(name: String, @ColorInt color: Int, ifExists: Boolean = true) =
        put(name, color, night = true, ifExists = ifExists)

    fun dayNight(name: String, @ColorInt current: Int, @ColorInt dark: Int, ifExists: Boolean = true) {
        color(name, current, ifExists)
        night(name, dark, ifExists)
    }

    companion object {
        private const val NIGHT_CONFIGURATION = "night"

        fun of(overlay: FabricatedOverlayResource): ColorSink = ColorSink { name, color, night, ifExists ->
            if (night) {
                overlay.setColor(name, color, NIGHT_CONFIGURATION, ifExists)
            } else {
                overlay.setColor(name, color, ifExists)
            }
        }

        fun collecting(target: MutableMap<String, Int>): ColorSink = ColorSink { name, color, night, _ ->
            if (!night) target[name] = color
        }
    }
}
