package com.drdisagree.colorblendr.utils.manager.backend

import android.content.om.IOverlayManager
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Utilities.getOneUiPaletteBackup
import com.drdisagree.colorblendr.data.common.Utilities.setOneUiPaletteBackup
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage.shellQuoted
import com.drdisagree.colorblendr.service.OneUiPaletteCaller
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme

object OneUiColorPalette {

    private const val TAG = "OneUiColorPalette"
    private const val STATE = "wallpapertheme_state"
    private const val COLORS = "wallpapertheme_color"
    private const val GOOGLE_COLORS = "wallpapertheme_color_for_g"
    private const val IS_GRAY = "wallpapertheme_color_isgray"
    private const val THEME_CUSTOMIZATION = "theme_customization_overlay_packages"
    private const val STATE_ENABLED = "1"
    private const val UNSET = "null"

    private val ROLES = listOf(
        PaletteRole.PRIMARY,
        PaletteRole.SECONDARY,
        PaletteRole.TERTIARY,
        PaletteRole.NEUTRAL,
        PaletteRole.NEUTRAL_VARIANT
    )
    private val PALETTE_SIZE = ROLES.size * ShadeScale.shades.size

    val isSupported: Boolean by lazy {
        runCatching {
            IOverlayManager::class.java.getMethod(
                OneUiPaletteCaller.METHOD_NAME,
                List::class.java,
                List::class.java,
                Boolean::class.javaPrimitiveType
            )
        }.isSuccess
    }

    fun apply(scheme: ResolvedScheme, isGray: Boolean, shell: (String) -> String) {
        if (!isSupported) return

        val colors = ROLES.flatMap { role -> ShadeScale.indices.map { index -> scheme[role, index] } }
        val expected = colors.toString()
        val grayFlag = if (isGray) "1" else "0"
        if (isApplied(expected, grayFlag, shell)) return

        if (getOneUiPaletteBackup() == null) setOneUiPaletteBackup(readBackup(shell))

        val csv = colors.joinToString(",")
        val called = call("${OneUiPaletteCaller.ACTION_APPLY} $csv $csv $isGray", shell)
        if (called && !isApplied(expected, grayFlag, shell)) {
            report("palette was not applied")
        }
    }

    fun restore(shell: (String) -> String) {
        if (!isSupported) return
        val backup = getOneUiPaletteBackup() ?: return

        val colors = backup.colors?.let(::parseColors)
        val googleColors = backup.googleColors?.let(::parseColors)
        val restored = if (backup.state == STATE_ENABLED && colors != null && googleColors != null) {
            call(
                "${OneUiPaletteCaller.ACTION_APPLY} ${colors.joinToString(",")} " +
                        "${googleColors.joinToString(",")} ${backup.isGray == "1"}",
                shell
            )
        } else {
            call(OneUiPaletteCaller.ACTION_CLEAR, shell).also { cleared ->
                if (cleared) restoreThemeCustomization(backup.themeCustomization, shell)
            }
        }
        if (restored) setOneUiPaletteBackup(null)
    }

    private fun isApplied(expected: String, grayFlag: String, shell: (String) -> String): Boolean =
        readSetting("system", STATE, shell) == STATE_ENABLED &&
                readSetting("system", COLORS, shell) == expected &&
                readSetting("system", IS_GRAY, shell) == grayFlag

    private fun readBackup(shell: (String) -> String) = OneUiPaletteBackup(
        state = readSetting("system", STATE, shell),
        colors = readSetting("system", COLORS, shell),
        googleColors = readSetting("system", GOOGLE_COLORS, shell),
        isGray = readSetting("system", IS_GRAY, shell),
        themeCustomization = readSetting("secure", THEME_CUSTOMIZATION, shell)
    )

    private fun restoreThemeCustomization(value: String?, shell: (String) -> String) {
        if (value == null) {
            shell("settings delete secure $THEME_CUSTOMIZATION")
        } else {
            shell("settings put secure $THEME_CUSTOMIZATION ${value.shellQuoted()}")
        }
    }

    private fun call(arguments: String, shell: (String) -> String): Boolean {
        val apk = appContext.applicationInfo.sourceDir.shellQuoted()
        val output = shell(
            "CLASSPATH=$apk app_process /system/bin " +
                    "${OneUiPaletteCaller::class.java.name} $arguments 2>&1; echo \"exit=$?\""
        )
        if (OneUiPaletteCaller.RESULT_OK in output) return true
        report(output.lines().filter { it.isNotBlank() }.takeLast(4).joinToString("\n"))
        return false
    }

    private fun readSetting(namespace: String, key: String, shell: (String) -> String): String? =
        shell("settings get $namespace $key").trim().takeIf { it.isNotEmpty() && it != UNSET }

    private fun parseColors(value: String): List<Int>? =
        value.removePrefix("[").removeSuffix("]")
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .takeIf { it.size == PALETTE_SIZE }

    private fun report(detail: String) {
        Log.e(TAG, "One UI color palette: $detail")
        OverlayErrors.reportOperation("One UI color palette: $detail")
    }
}
