package com.drdisagree.colorblendr.theme.overlay.writer

import com.drdisagree.colorblendr.theme.overlay.ColorSink
import com.drdisagree.colorblendr.theme.overlay.OverlayTarget
import com.drdisagree.colorblendr.theme.overlay.ResourceNames
import com.drdisagree.colorblendr.theme.overlay.ThemeColorWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.TokenGroup

object MaterialTokenWriter : ThemeColorWriter {

    private val SYSTEM_COLOR_PREFIXES = listOf(
        "m3_sys_color_",
        "m3_sys_color_dynamic_",
        "gm3_sys_color_",
        "gm3_sys_color_dynamic_"
    )
    private val CURRENT_MODE_PREFIXES = listOf("media_dialog_", "media_")
    private val PASCAL_PREFIXES = listOf("settingslib_materialColor", "materialColor")
    private const val CUSTOM_PASCAL_PREFIX = "customColor"

    override fun write(theme: ResolvedTheme, target: OverlayTarget, sink: ColorSink) {
        val current = theme.scheme(target.isDarkMode)

        current.tokens(TokenGroup.SYSTEM).forEach { token ->
            listOf(theme.light, theme.dark).forEach { scheme ->
                val mode = ResourceNames.mode(scheme.isDark)
                SYSTEM_COLOR_PREFIXES.forEach { prefix ->
                    sink.color("$prefix${mode}_${token.resourceName}", scheme[token])
                }
            }

            val light = theme.light[token]
            val dark = theme.dark[token]
            CURRENT_MODE_PREFIXES.forEach { prefix ->
                sink.dayNight("$prefix${token.resourceName}", light, dark, target.isDarkMode)
            }
            PASCAL_PREFIXES.forEach { prefix ->
                sink.dayNight("$prefix${ResourceNames.pascal(token.resourceName)}", light, dark, target.isDarkMode)
            }
        }

        current.tokens(TokenGroup.FIXED).forEach { token ->
            SYSTEM_COLOR_PREFIXES.forEach { prefix ->
                sink.dayNight("$prefix${token.resourceName}", theme.light[token], theme.dark[token], target.isDarkMode)
            }
            PASCAL_PREFIXES.forEach { prefix ->
                val name = "$prefix${ResourceNames.pascal(token.resourceName)}"
                sink.dayNight(name, theme.light[token], theme.dark[token], target.isDarkMode)
                sink.color("${name}Light", theme.light[token])
                sink.color("${name}Dark", theme.dark[token])
            }
        }

        current.tokens(TokenGroup.CUSTOM).forEach { token ->
            sink.dayNight(
                "$CUSTOM_PASCAL_PREFIX${ResourceNames.pascal(token.resourceName)}",
                theme.light[token],
                theme.dark[token],
                target.isDarkMode
            )
        }
    }
}
