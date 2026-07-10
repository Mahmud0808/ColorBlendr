package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.utils.colors.ColorModifiers
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.generateColorPalette
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import java.util.concurrent.atomic.AtomicInteger

// Pref-free palette derivation for community themes: same pipeline the
// overlay uses, but every input comes from the payload so any theme renders
// identically on any device (spec version stays device-local by design).
object CommunityThemePalette {

    fun derive(theme: CommunityTheme, isDark: Boolean): ArrayList<ArrayList<Int>> {
        val palette = generateColorPalette(theme.style, theme.seedColor, isDark)

        theme.secondaryColor?.let {
            palette[1] = generateColorPalette(theme.style, it, isDark)[0]
        }
        theme.tertiaryColor?.let {
            palette[2] = generateColorPalette(theme.style, it, isDark)[0]
        }

        for (i in palette.indices) {
            val modifiedShades = ColorModifiers.modifyColors(
                ArrayList(palette[i].subList(1, palette[i].size)),
                AtomicInteger(i),
                theme.style,
                theme.accentSaturation,
                theme.backgroundSaturation,
                theme.backgroundLightness,
                theme.pitchBlack,
                theme.accurateShades,
                modifyPitchBlack = true,
                overrideColors = false
            )
            for (j in 1 until palette[i].size) {
                palette[i][j] = modifiedShades[j - 1]
            }
        }

        theme.colorOverrides.forEach { (shadeName, color) ->
            systemPaletteNames.forEachIndexed { i, row ->
                val j = row.indexOf(shadeName)
                if (j >= 0 && i < palette.size && j < palette[i].size) {
                    palette[i][j] = color
                }
            }
        }

        return palette
    }
}
