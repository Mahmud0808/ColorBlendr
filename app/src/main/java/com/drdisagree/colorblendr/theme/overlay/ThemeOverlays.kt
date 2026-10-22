package com.drdisagree.colorblendr.theme.overlay

import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEMUI
import com.drdisagree.colorblendr.data.common.Constant.FRAMEWORK_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.theme.overlay.writer.AppColorOverrideWriter
import com.drdisagree.colorblendr.theme.overlay.writer.EosColorWriter
import com.drdisagree.colorblendr.theme.overlay.writer.FrameworkExtrasWriter
import com.drdisagree.colorblendr.theme.overlay.writer.MaterialTokenWriter
import com.drdisagree.colorblendr.theme.overlay.writer.PerPackageColorWriter
import com.drdisagree.colorblendr.theme.overlay.writer.ReferencePaletteWriter
import com.drdisagree.colorblendr.theme.overlay.writer.SurfaceEffectWriter
import com.drdisagree.colorblendr.theme.overlay.writer.SystemPaletteWriter
import com.drdisagree.colorblendr.theme.overlay.writer.SystemTokenWriter
import com.drdisagree.colorblendr.theme.overlay.writer.SystemUiExtrasWriter
import com.drdisagree.colorblendr.theme.overlay.writer.TintlessTextWriter
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

object ThemeOverlays {

    private val MATERIAL_WRITERS = listOf(
        SystemTokenWriter,
        MaterialTokenWriter,
        ReferencePaletteWriter,
        EosColorWriter
    )

    private val FRAMEWORK_WRITERS = listOf(SystemPaletteWriter) + MATERIAL_WRITERS +
            listOf(TintlessTextWriter, SurfaceEffectWriter, FrameworkExtrasWriter)

    private val SYSTEM_UI_WRITERS = MATERIAL_WRITERS + listOf(TintlessTextWriter, SystemUiExtrasWriter)

    private val PER_APP_THEME_WRITERS =
        MATERIAL_WRITERS + listOf(PerPackageColorWriter, TintlessTextWriter)

    private val PER_APP_WRITERS = PER_APP_THEME_WRITERS + AppColorOverrideWriter

    private val PRESET_WRITERS = listOf(PerPackageColorWriter)

    fun framework(theme: ResolvedTheme, isDarkMode: Boolean): FabricatedOverlayResource =
        overlay(FABRICATED_OVERLAY_NAME_SYSTEM, FRAMEWORK_PACKAGE, theme, isDarkMode, FRAMEWORK_WRITERS)

    fun systemUi(theme: ResolvedTheme, isDarkMode: Boolean): FabricatedOverlayResource =
        overlay(FABRICATED_OVERLAY_NAME_SYSTEMUI, SYSTEMUI_PACKAGE, theme, isDarkMode, SYSTEM_UI_WRITERS)
            .apply { setBoolean("flag_monet", false, ifExists = true) }

    fun perApp(packageName: String, theme: ResolvedTheme, isDarkMode: Boolean): FabricatedOverlayResource =
        overlay(
            String.format(FABRICATED_OVERLAY_NAME_APPS, packageName),
            packageName,
            theme,
            isDarkMode,
            PER_APP_WRITERS
        )

    fun presetColors(packageName: String, theme: ResolvedTheme, isDarkMode: Boolean): Map<String, Int> =
        collect(packageName, theme, isDarkMode, PRESET_WRITERS)

    fun themedColors(packageName: String, theme: ResolvedTheme, isDarkMode: Boolean): Map<String, Int> =
        collect(packageName, theme, isDarkMode, PER_APP_THEME_WRITERS)

    fun frameworkColors(theme: ResolvedTheme, isDarkMode: Boolean): Map<String, Int> =
        HashMap<String, Int>().also { colors ->
            write(theme, OverlayTarget(FRAMEWORK_PACKAGE, isDarkMode), ColorSink.collecting(colors), FRAMEWORK_WRITERS)
        }

    private fun collect(
        packageName: String,
        theme: ResolvedTheme,
        isDarkMode: Boolean,
        writers: List<ThemeColorWriter>
    ): Map<String, Int> = HashMap<String, Int>().also { colors ->
        write(theme, OverlayTarget(packageName, isDarkMode), ColorSink.collecting(colors), writers)
    }

    private fun overlay(
        overlayName: String,
        packageName: String,
        theme: ResolvedTheme,
        isDarkMode: Boolean,
        writers: List<ThemeColorWriter>
    ): FabricatedOverlayResource = FabricatedOverlayResource(overlayName, packageName).also { overlay ->
        write(theme, OverlayTarget(packageName, isDarkMode), ColorSink.of(overlay), writers)
    }

    private fun write(
        theme: ResolvedTheme,
        target: OverlayTarget,
        sink: ColorSink,
        writers: List<ThemeColorWriter>
    ) = writers.forEach { it.write(theme, target, sink) }
}
