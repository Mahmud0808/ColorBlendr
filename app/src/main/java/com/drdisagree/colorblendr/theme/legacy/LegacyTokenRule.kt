package com.drdisagree.colorblendr.theme.legacy

import android.graphics.Color
import com.drdisagree.colorblendr.theme.palette.AdjustedPalette
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.PaletteRole.ERROR
import com.drdisagree.colorblendr.theme.model.PaletteRole.NEUTRAL
import com.drdisagree.colorblendr.theme.model.PaletteRole.NEUTRAL_VARIANT
import com.drdisagree.colorblendr.theme.model.PaletteRole.PRIMARY
import com.drdisagree.colorblendr.theme.model.PaletteRole.SECONDARY
import com.drdisagree.colorblendr.theme.model.PaletteRole.TERTIARY
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.rule.ThemeRule
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenResolution
import com.drdisagree.materialcolorutilities.hct.Hct

class LegacyTokenRule(
    private val roles: Set<PaletteRole>,
    private val palettes: Map<PaletteRole, AdjustedPalette>,
    private val pitchBlack: Boolean
) : ThemeRule {

    override fun token(token: Token, resolution: TokenResolution, isDark: Boolean): TokenResolution {
        val source = SOURCES[token]?.takeIf { it.role in roles } ?: return resolution
        val shade = shade(source.role, source.index(isDark))
        val hct = Hct.fromInt(shade)
        val percent = source.adjustment(isDark).coerceIn(-100, 100)
        val tone = (hct.tone + hct.tone * percent / 100.0).coerceIn(0.0, 100.0)
        val color = if (percent == 0) shade else Hct.from(hct.hue, hct.chroma, tone).toInt()
        return resolution.copy(role = source.role, tone = tone, chromaMultiplier = 1.0, color = color)
    }

    private fun shade(role: PaletteRole, index: Int): Int {
        if (pitchBlack && role.isNeutral && index == ShadeScale.SHADE_900) return Color.BLACK
        return palettes.getValue(role).shades[index]
    }

    companion object {
        fun rolesOf(shadeOverrides: Map<String, Int>): Set<PaletteRole> =
            shadeOverrides.keys.mapNotNull { name ->
                PaletteRole.entries.firstOrNull { name.startsWith(it.systemName + "_") }
            }.toSet()

        private val SOURCES: Map<Token, LegacyTokenSource> = mapOf(
            Token.PRIMARY_CONTAINER to source(PRIMARY, 3, 9),
            Token.ON_PRIMARY_CONTAINER to source(PRIMARY, 11, 3),
            Token.PRIMARY to source(PRIMARY, 8, 4),
            Token.ON_PRIMARY to source(PRIMARY, 0, 10),
            Token.SECONDARY_CONTAINER to source(SECONDARY, 3, 9),
            Token.ON_SECONDARY_CONTAINER to source(SECONDARY, 11, 3),
            Token.SECONDARY to source(SECONDARY, 8, 4),
            Token.ON_SECONDARY to source(SECONDARY, 0, 10),
            Token.TERTIARY_CONTAINER to source(TERTIARY, 3, 9),
            Token.ON_TERTIARY_CONTAINER to source(TERTIARY, 11, 3),
            Token.TERTIARY to source(TERTIARY, 8, 4),
            Token.ON_TERTIARY to source(TERTIARY, 0, 10),
            Token.BACKGROUND to source(NEUTRAL, 1, 11, -1, -25),
            Token.ON_BACKGROUND to source(NEUTRAL, 11, 1),
            Token.SURFACE to source(NEUTRAL, 1, 11, -1, -25),
            Token.ON_SURFACE to source(NEUTRAL, 10, 2),
            Token.SURFACE_CONTAINER_LOWEST to source(NEUTRAL, 2, 10, 3, -49),
            Token.SURFACE_CONTAINER_LOW to source(NEUTRAL, 2, 10, 1, -48),
            Token.SURFACE_CONTAINER to source(NEUTRAL, 2, 10, -2, -42),
            Token.SURFACE_CONTAINER_HIGH to source(NEUTRAL, 1, 10, -4, 0),
            Token.SURFACE_CONTAINER_HIGHEST to source(NEUTRAL, 1, 10, -5, 3),
            Token.SURFACE_BRIGHT to source(NEUTRAL, 1, 10, -2, 13),
            Token.SURFACE_DIM to source(NEUTRAL, 1, 11, -9, 7),
            Token.SURFACE_VARIANT to source(NEUTRAL_VARIANT, 2, 10),
            Token.ON_SURFACE_VARIANT to source(NEUTRAL_VARIANT, 10, 2),
            Token.OUTLINE to source(NEUTRAL_VARIANT, 7, 6),
            Token.OUTLINE_VARIANT to source(NEUTRAL_VARIANT, 4, 9),
            Token.ERROR to source(ERROR, 8, 4),
            Token.ON_ERROR to source(ERROR, 3, 9),
            Token.ERROR_CONTAINER to source(ERROR, 3, 9),
            Token.ON_ERROR_CONTAINER to source(ERROR, 8, 4),
            Token.CONTROL_ACTIVATED to source(SECONDARY, 8, 4),
            Token.CONTROL_NORMAL to source(NEUTRAL_VARIANT, 4, 9),
            Token.TEXT_PRIMARY_INVERSE to source(NEUTRAL, 3, 11),
            Token.TEXT_SECONDARY_AND_TERTIARY_INVERSE to source(NEUTRAL_VARIANT, 4, 9),
            Token.TEXT_PRIMARY_INVERSE_DISABLE_ONLY to source(NEUTRAL, 3, 11),
            Token.TEXT_SECONDARY_AND_TERTIARY_INVERSE_DISABLED to source(NEUTRAL, 3, 11),
            Token.TEXT_HINT_INVERSE to source(NEUTRAL, 3, 11),
            Token.PALETTE_KEY_COLOR_PRIMARY to source(PRIMARY, 4, 8),
            Token.PALETTE_KEY_COLOR_SECONDARY to source(SECONDARY, 4, 8),
            Token.PALETTE_KEY_COLOR_TERTIARY to source(TERTIARY, 4, 8),
            Token.PALETTE_KEY_COLOR_NEUTRAL to source(NEUTRAL, 4, 8),
            Token.PALETTE_KEY_COLOR_NEUTRAL_VARIANT to source(NEUTRAL_VARIANT, 4, 8),
            Token.PRIMARY_DIM to source(PRIMARY, 7, 5),
            Token.SECONDARY_DIM to source(SECONDARY, 7, 5),
            Token.TERTIARY_DIM to source(TERTIARY, 7, 5),
            Token.INVERSE_SURFACE to source(NEUTRAL, 11, 1, -25, -1),
            Token.INVERSE_ON_SURFACE to source(NEUTRAL, 2, 10),
            Token.INVERSE_PRIMARY to source(PRIMARY, 4, 8),
            Token.SURFACE_TINT to source(PRIMARY, 8, 4),
            Token.GEN_WIDGET to source(PRIMARY, 4, 8),
            Token.PRIMARY_FIXED to source(PRIMARY, 3, 3),
            Token.PRIMARY_FIXED_DIM to source(PRIMARY, 4, 4),
            Token.ON_PRIMARY_FIXED to source(PRIMARY, 11, 11),
            Token.ON_PRIMARY_FIXED_VARIANT to source(PRIMARY, 9, 9),
            Token.SECONDARY_FIXED to source(SECONDARY, 3, 3),
            Token.SECONDARY_FIXED_DIM to source(SECONDARY, 4, 4),
            Token.ON_SECONDARY_FIXED to source(SECONDARY, 11, 11),
            Token.ON_SECONDARY_FIXED_VARIANT to source(SECONDARY, 9, 9),
            Token.TERTIARY_FIXED to source(TERTIARY, 4, 4),
            Token.TERTIARY_FIXED_DIM to source(TERTIARY, 4, 4),
            Token.ON_TERTIARY_FIXED to source(TERTIARY, 11, 11),
            Token.ON_TERTIARY_FIXED_VARIANT to source(TERTIARY, 9, 9),
            Token.WIDGET_BACKGROUND to source(PRIMARY, 2, 10),
            Token.CLOCK_HOUR to source(SECONDARY, 6, 9),
            Token.CLOCK_MINUTE to source(PRIMARY, 3, 8),
            Token.CLOCK_SECOND to source(TERTIARY, 3, 8),
            Token.WEATHER_TEMP to source(PRIMARY, 4, 6, 0, -42),
            Token.THEME_APP to source(PRIMARY, 3, 9),
            Token.ON_THEME_APP to source(PRIMARY, 8, 4),
            Token.THEME_APP_RING to source(PRIMARY, 5, 5),
            Token.THEME_NOTIF to source(TERTIARY, 4, 3),
            Token.BRAND_A to source(PRIMARY, 8, 4),
            Token.BRAND_B to source(SECONDARY, 6, 3, -16, 18),
            Token.BRAND_C to source(PRIMARY, 7, 6),
            Token.BRAND_D to source(TERTIARY, 6, 3, -14, 18),
            Token.UNDER_SURFACE to source(NEUTRAL, 12, 12),
            Token.SHADE_ACTIVE to source(PRIMARY, 3, 3),
            Token.ON_SHADE_ACTIVE to source(PRIMARY, 11, 11),
            Token.ON_SHADE_ACTIVE_VARIANT to source(PRIMARY, 9, 9),
            Token.SHADE_INACTIVE to source(NEUTRAL, 10, 10),
            Token.ON_SHADE_INACTIVE to source(NEUTRAL, 3, 3),
            Token.ON_SHADE_INACTIVE_VARIANT to source(NEUTRAL_VARIANT, 4, 4),
            Token.SHADE_DISABLED to source(NEUTRAL, 11, 11),
            Token.OVERVIEW_BACKGROUND to source(NEUTRAL_VARIANT, 4, 8, 0, -42)
        )

        private fun source(
            role: PaletteRole,
            lightIndex: Int,
            darkIndex: Int,
            lightAdjustment: Int = 0,
            darkAdjustment: Int = 0
        ) = LegacyTokenSource(role, lightIndex, darkIndex, lightAdjustment, darkAdjustment)
    }
}
