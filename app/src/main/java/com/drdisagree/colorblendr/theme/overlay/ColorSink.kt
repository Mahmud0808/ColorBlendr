package com.drdisagree.colorblendr.theme.overlay

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

fun interface ColorSink {

    fun put(name: String, @ColorInt color: Int, configuration: String?, ifExists: Boolean)

    fun color(name: String, @ColorInt color: Int, ifExists: Boolean = true) =
        put(name, color, configuration = null, ifExists = ifExists)

    fun night(name: String, @ColorInt color: Int, ifExists: Boolean = true) =
        put(name, color, NIGHT_CONFIGURATION, ifExists)

    fun notNight(name: String, @ColorInt color: Int, ifExists: Boolean = true) =
        put(name, color, NOT_NIGHT_CONFIGURATION, ifExists)

    fun dayNight(name: String, @ColorInt current: Int, @ColorInt dark: Int, ifExists: Boolean = true) {
        color(name, current, ifExists)
        night(name, dark, ifExists)
    }

    companion object {
        private const val NIGHT_CONFIGURATION = "night"
        private const val NOT_NIGHT_CONFIGURATION = "notnight"

        fun of(overlay: FabricatedOverlayResource): ColorSink =
            ColorSink { name, color, configuration, ifExists ->
                overlay.setColor(name, color, configuration, ifExists)
            }

        fun collecting(target: MutableMap<String, Int>): ColorSink =
            ColorSink { name, color, configuration, _ ->
                if (configuration == null) target[name] = color
            }
    }
}
