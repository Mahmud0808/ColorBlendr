package com.drdisagree.colorblendr.theme.overlay.writer

import android.os.Build
import com.drdisagree.colorblendr.data.common.Utilities.getAppColorOverrides
import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme

object AppColorOverrideWriter : ThemeColorWriter {

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val overrides = getAppColorOverrides(target.packageName)
        if (overrides.isEmpty) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrides.light.forEach { (name, override) ->
                sink.notNight(name, theme.light.colorOf(override))
            }
            overrides.dark.forEach { (name, override) ->
                sink.night(name, theme.dark.colorOf(override))
            }
        } else {
            val scheme = theme.scheme(target.isDarkMode)
            overrides.forMode(target.isDarkMode).forEach { (name, override) ->
                sink.color(name, scheme.colorOf(override))
            }
        }
    }
}
