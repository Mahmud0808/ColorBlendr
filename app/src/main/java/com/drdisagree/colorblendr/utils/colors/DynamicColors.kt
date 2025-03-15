package com.drdisagree.colorblendr.utils.colors

import android.os.Build
import androidx.core.graphics.toColorInt
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.enums.MONET

enum class TonalPalette(val index: Int) {
    PRIMARY(0),
    SECONDARY(1),
    TERTIARY(2),
    NEUTRAL(3),
    NEUTRAL_VARIANT(4)
}

/*
 * Source: https://cs.android.com/android/platform/superproject/main/+/main:frameworks/libs/systemui/monet/src/com/android/systemui/monet/DynamicColors.java
 */
object DynamicColors {

    val ALL_DYNAMIC_COLORS_MAPPED: MutableList<ColorMapping> get() = getAllDynamicColorsMapped()
    val FIXED_COLORS_MAPPED: MutableList<ColorMapping> get() = getAllFixedColorsMapped()
    val CUSTOM_COLORS_MAPPED: MutableList<ColorMapping> get() = getAllCustomColorsMapped()
    val M3_REF_PALETTE: MutableList<ColorMapping> get() = getAllM3RefPalette()

    /*
     * List of all public Dynamic Color (Light and Dark) resources
     */
    private fun getAllDynamicColorsMapped(): MutableList<ColorMapping> {
        return mutableListOf(
            ColorMapping(
                resourceName = "primary_container",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "on_primary_container",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 11,
                darkModeColorIndex = 3
            ),
            ColorMapping(
                resourceName = "primary",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_primary",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 0,
                darkModeColorIndex = 10
            ),
            ColorMapping(
                resourceName = "secondary_container",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "on_secondary_container",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 11,
                darkModeColorIndex = 3
            ),
            ColorMapping(
                resourceName = "secondary",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_secondary",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 0,
                darkModeColorIndex = 10
            ),
            ColorMapping(
                resourceName = "tertiary_container",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "on_tertiary_container",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 11,
                darkModeColorIndex = 3
            ),
            ColorMapping(
                resourceName = "tertiary",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_tertiary",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 0,
                darkModeColorIndex = 10
            ),
            ColorMapping(
                resourceName = "background",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 11,
                lightModeLightnessAdjustment = -1,
                darkModeLightnessAdjustment = -25
            ),
            ColorMapping(
                resourceName = "on_background",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 11,
                darkModeColorIndex = 1
            ),
            ColorMapping(
                resourceName = "surface",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 11,
                lightModeLightnessAdjustment = -1,
                darkModeLightnessAdjustment = -25
            ),
            ColorMapping(
                resourceName = "on_surface",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 10,
                darkModeColorIndex = 2
            ),
            ColorMapping(
                resourceName = "surface_container_lowest",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 2,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = 3,
                darkModeLightnessAdjustment = -49
            ),
            ColorMapping(
                resourceName = "surface_container_low",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 2,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = 1,
                darkModeLightnessAdjustment = -48
            ),
            ColorMapping(
                resourceName = "surface_container",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 2,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = -2,
                darkModeLightnessAdjustment = -42
            ),
            ColorMapping(
                resourceName = "surface_container_high", // android 14+ notification bg color
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = -4
            ),
            ColorMapping(
                resourceName = "surface_container_highest",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = -5,
                darkModeLightnessAdjustment = 3
            ),
            ColorMapping(
                resourceName = "surface_bright",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 10,
                lightModeLightnessAdjustment = -2,
                darkModeLightnessAdjustment = 13
            ),
            ColorMapping(
                resourceName = "surface_dim", // android 14+ notification scrim color
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 1,
                darkModeColorIndex = 11,
                lightModeLightnessAdjustment = -9,
                darkModeLightnessAdjustment = 7
            ),
            ColorMapping(
                resourceName = "surface_variant",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 2,
                darkModeColorIndex = 10
            ),
            ColorMapping(
                resourceName = "on_surface_variant",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 10,
                darkModeColorIndex = 2
            ),
            ColorMapping(
                resourceName = "outline",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 7,
                darkModeColorIndex = 6
            ),
            ColorMapping(
                resourceName = "outline_variant",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 4,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "error",
                colorCode = "#FF5540".toColorInt(),
                lightModeLStarAdjustment = 40.0,
                darkModeLStarAdjustment = 80.0
            ),
            ColorMapping(
                resourceName = "on_error",
                colorCode = "#FF5540".toColorInt(),
                lightModeLStarAdjustment = 100.0,
                darkModeLStarAdjustment = 20.0
            ),
            ColorMapping(
                resourceName = "error_container",
                colorCode = "#FF5540".toColorInt(),
                lightModeLStarAdjustment = 90.0,
                darkModeLStarAdjustment = 30.0
            ),
            ColorMapping(
                resourceName = "on_error_container",
                colorCode = "#FF5540".toColorInt(),
                lightModeLStarAdjustment = if (getCurrentMonetStyle() == MONET.MONOCHROMATIC) 10.0 else 30.0,
                darkModeLStarAdjustment = 90.0
            ),
            ColorMapping(
                resourceName = "control_activated",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "control_normal",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 4,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "control_highlight",
                lightModeColorCode = "#1F000000".toColorInt(),
                darkModeColorCode = "#33FFFFFF".toColorInt()
            ),
            ColorMapping(
                resourceName = "text_primary_inverse",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 3,
                darkModeColorIndex = 11
            ),
            ColorMapping(
                resourceName = "text_secondary_and_tertiary_inverse",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 4,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "text_primary_inverse_disable_only",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 3,
                darkModeColorIndex = 11
            ),
            ColorMapping(
                resourceName = "text_secondary_and_tertiary_inverse_disabled",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 3,
                darkModeColorIndex = 11
            ),
            ColorMapping(
                resourceName = "text_hint_inverse",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 3,
                darkModeColorIndex = 11
            ),
            ColorMapping(
                resourceName = "palette_key_color_primary",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "palette_key_color_secondary",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "palette_key_color_tertiary",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "palette_key_color_neutral",
                tonalPalette = TonalPalette.NEUTRAL,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "palette_key_color_neutral_variant",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8
            )
        )
    }

    /**
     * List of all public Static Color resources
     */
    private fun getAllFixedColorsMapped(): MutableList<ColorMapping> {
        return mutableListOf(
            ColorMapping(
                resourceName = "primary_fixed",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "primary_fixed_dim",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_primary_fixed",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "on_primary_fixed_variant",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "secondary_fixed",
                tonalPalette = TonalPalette.SECONDARY,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "secondary_fixed_dim",
                tonalPalette = TonalPalette.SECONDARY,
                colorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_secondary_fixed",
                tonalPalette = TonalPalette.SECONDARY,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "on_secondary_fixed_variant",
                tonalPalette = TonalPalette.SECONDARY,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "tertiary_fixed",
                tonalPalette = TonalPalette.TERTIARY,
                colorIndex = 4
            ),
            ColorMapping(
                resourceName = "tertiary_fixed_dim",
                tonalPalette = TonalPalette.TERTIARY,
                colorIndex = 4
            ),
            ColorMapping(
                resourceName = "on_tertiary_fixed",
                tonalPalette = TonalPalette.TERTIARY,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "on_tertiary_fixed_variant",
                tonalPalette = TonalPalette.TERTIARY,
                colorIndex = 9
            )
        )
    }

    /*
     * List of all private SystemUI Color resources
     */
    private fun getAllCustomColorsMapped(): MutableList<ColorMapping> {
        // New resources which are added on Android 15 QPR1 Beta 3
        if (Build.VERSION.SDK_INT < 35) return mutableListOf()

        return mutableListOf(
            // CLOCK COLORS
            ColorMapping(
                resourceName = "widget_background",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 2,
                darkModeColorIndex = 10
            ),
            ColorMapping(
                resourceName = "clock_hour",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 6,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "clock_minute",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "clock_second",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 8
            ),
            ColorMapping(
                resourceName = "weather_temp",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 4,
                darkModeColorIndex = 6,
                darkModeLightnessAdjustment = -42
            ),

            // THEME APP ICONS
            ColorMapping(
                resourceName = "theme_app",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 3,
                darkModeColorIndex = 9
            ),
            ColorMapping(
                resourceName = "on_theme_app",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "theme_app_ring",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 5
            ),
            ColorMapping(
                resourceName = "theme_notif",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 4,
                darkModeColorIndex = 3
            ),

            // SUPER G COLORS
            ColorMapping(
                resourceName = "brand_a",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 8,
                darkModeColorIndex = 4
            ),
            ColorMapping(
                resourceName = "brand_b",
                tonalPalette = TonalPalette.SECONDARY,
                lightModeColorIndex = 6,
                darkModeColorIndex = 3,
                lightModeLightnessAdjustment = -16,
                darkModeLightnessAdjustment = 18
            ),
            ColorMapping(
                resourceName = "brand_c",
                tonalPalette = TonalPalette.PRIMARY,
                lightModeColorIndex = 7,
                darkModeColorIndex = 6
            ),
            ColorMapping(
                resourceName = "brand_d",
                tonalPalette = TonalPalette.TERTIARY,
                lightModeColorIndex = 6,
                darkModeColorIndex = 3,
                lightModeLightnessAdjustment = -14,
                darkModeLightnessAdjustment = 18
            ),

            // QUICK SETTING TILES
            ColorMapping(
                resourceName = "under_surface",
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 12
            ),
            ColorMapping(
                resourceName = "shade_active",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "on_shade_active",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "on_shade_active_variant",
                tonalPalette = TonalPalette.PRIMARY,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "shade_inactive",
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 10
            ),
            ColorMapping(
                resourceName = "on_shade_inactive",
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "on_shade_inactive_variant",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 4
            ),
            ColorMapping(
                resourceName = "shade_disabled",
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "overview_background",
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                lightModeColorIndex = 4,
                darkModeColorIndex = 8,
                darkModeLightnessAdjustment = -42
            )
        )
    }

    /*
     * Google uses the "gm3" prefix for these resources,
     * while MaterialComponents uses "m3" instead.
     */
    private fun getAllM3RefPalette(): MutableList<ColorMapping> {
        return mutableListOf(
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral4",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral6",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral12",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 10
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral17",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 10
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral22",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral24",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral87",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral92",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 2
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral94",
                lightnessAdjustment = -2,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 2
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral96",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 1
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral98",
                lightnessAdjustment = -2,
                tonalPalette = TonalPalette.NEUTRAL,
                colorIndex = 1
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant4",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant6",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 11
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant12",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 10
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant17",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 10
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant22",
                lightnessAdjustment = -26,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant24",
                lightnessAdjustment = -9,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 9
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant87",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 3
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant92",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 2
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant94",
                lightnessAdjustment = -2,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 2
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant96",
                lightnessAdjustment = -4,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 1
            ),
            ColorMapping(
                resourceName = "m3_ref_palette_dynamic_neutral_variant98",
                lightnessAdjustment = -2,
                tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                colorIndex = 1
            )
        )
    }
}
