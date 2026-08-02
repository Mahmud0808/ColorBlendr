package com.drdisagree.colorblendr.utils.community

import android.util.LruCache
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.utils.colors.ColorModifiers
import com.drdisagree.colorblendr.utils.colors.ColorSchemeUtil.generateColorPalette
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import java.util.concurrent.atomic.AtomicInteger

// Pref-free palette derivation for community themes: same pipeline the
// overlay uses, but every input comes from the payload so any theme renders
// identically on any device, including the payload's color spec version.
object CommunityThemePalette {

    private val cache = LruCache<String, ArrayList<ArrayList<Int>>>(32)

    fun derive(theme: CommunityTheme, isDark: Boolean): ArrayList<ArrayList<Int>> {
        val key = "${theme.id}:${theme.createdAt}:$isDark"
        cache.get(key)?.let { cached ->
            return ArrayList(cached.map { ArrayList(it) })
        }
        val palette = deriveUncached(theme, isDark)
        cache.put(key, ArrayList(palette.map { ArrayList(it) }))
        return palette
    }

    private fun deriveUncached(theme: CommunityTheme, isDark: Boolean): ArrayList<ArrayList<Int>> {
        val palette = generateColorPalette(
            theme.style,
            theme.seedColor,
            isDark,
            colorSpecVersion = theme.colorSpecVersion
        )

        theme.secondaryColor?.let {
            palette[1] = generateColorPalette(
                theme.style, it, isDark, colorSpecVersion = theme.colorSpecVersion
            )[0]
        }
        theme.tertiaryColor?.let {
            palette[2] = generateColorPalette(
                theme.style, it, isDark, colorSpecVersion = theme.colorSpecVersion
            )[0]
        }

        // Light mode may carry its own slider values.
        val lightMode = !isDark && theme.modeSpecificThemes
        val accentSaturation =
            if (lightMode) theme.accentSaturationLight else theme.accentSaturation
        val backgroundSaturation =
            if (lightMode) theme.backgroundSaturationLight else theme.backgroundSaturation
        val backgroundLightness =
            if (lightMode) theme.backgroundLightnessLight else theme.backgroundLightness

        for (i in palette.indices) {
            val modifiedShades = ColorModifiers.modifyColors(
                ArrayList(palette[i].subList(1, palette[i].size)),
                AtomicInteger(i),
                theme.style,
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
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
