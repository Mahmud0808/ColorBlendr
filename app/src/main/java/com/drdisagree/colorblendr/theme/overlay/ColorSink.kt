package com.drdisagree.colorblendr.theme.overlay

import android.os.Build
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

    fun dayNight(
        name: String,
        @ColorInt light: Int,
        @ColorInt dark: Int,
        isDark: Boolean,
        ifExists: Boolean = true
    ) {
        val qualifiersSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        color(name, if (isDark && !qualifiersSupported) dark else light, ifExists)
        night(name, dark, ifExists)
    }

    companion object {
        private const val NIGHT_CONFIGURATION = "night"
        private const val NOT_NIGHT_CONFIGURATION = "notnight"

        fun of(overlay: FabricatedOverlayResource): ColorSink =
            ColorSink { name, color, configuration, ifExists ->
                overlay.setColor(name, color, configuration, ifExists)
            }

        fun collecting(target: MutableMap<String, Int>, isDark: Boolean): ColorSink {
            val qualified = HashSet<String>()
            val activeConfiguration = if (isDark) NIGHT_CONFIGURATION else NOT_NIGHT_CONFIGURATION
            return ColorSink { name, color, configuration, _ ->
                when (configuration) {
                    null -> if (name !in qualified) target[name] = color
                    activeConfiguration -> {
                        target[name] = color
                        qualified += name
                    }
                }
            }
        }
    }
}
