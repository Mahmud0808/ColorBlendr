package com.drdisagree.colorblendr.utils

import android.graphics.Color
import android.os.Build

enum class TonalPalette(val index: Int) {
    PRIMARY(0),
    SECONDARY(1),
    TERTIARY(2),
    NEUTRAL(3),
    NEUTRAL_VARIANT(4)
}

object DynamicColors {

    val ALL_DYNAMIC_COLORS_MAPPED: MutableList<ColorMapping> = ArrayList()
    val FIXED_COLORS_MAPPED: MutableList<ColorMapping> = ArrayList()
    val M3_REF_PALETTE: MutableList<ColorMapping> = ArrayList()

    /*
     * This is a list of all the dynamic and fixed colors that are available in the system.
     */
    init {
        ALL_DYNAMIC_COLORS_MAPPED.apply {
            add(
                ColorMapping(
                    resourceName = "primary_container",
                    tonalPalette = TonalPalette.PRIMARY,
                    lightModeColorIndex = 3,
                    darkModeColorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_primary_container",
                    tonalPalette = TonalPalette.PRIMARY,
                    lightModeColorIndex = 11,
                    darkModeColorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "primary",
                    tonalPalette = TonalPalette.PRIMARY,
                    lightModeColorIndex = 8,
                    darkModeColorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_primary",
                    tonalPalette = TonalPalette.PRIMARY,
                    lightModeColorIndex = 0,
                    darkModeColorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "secondary_container",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 3,
                    darkModeColorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_secondary_container",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 11,
                    darkModeColorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "secondary",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 8,
                    darkModeColorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_secondary",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 0,
                    darkModeColorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "tertiary_container",
                    tonalPalette = TonalPalette.TERTIARY,
                    lightModeColorIndex = 3,
                    darkModeColorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_tertiary_container",
                    tonalPalette = TonalPalette.TERTIARY,
                    lightModeColorIndex = 11,
                    darkModeColorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "tertiary",
                    tonalPalette = TonalPalette.TERTIARY,
                    lightModeColorIndex = 8,
                    darkModeColorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_tertiary",
                    tonalPalette = TonalPalette.TERTIARY,
                    lightModeColorIndex = 0,
                    darkModeColorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "background",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 11,
                    lightModeLightnessAdjustment = -1,
                    darkModeLightnessAdjustment = -25
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_background",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 11,
                    darkModeColorIndex = 1
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 11,
                    lightModeLightnessAdjustment = -1,
                    darkModeLightnessAdjustment = -25
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_surface",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 10,
                    darkModeColorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_container_lowest",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 2,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = 3,
                    darkModeLightnessAdjustment = -49
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_container_low",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 2,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = 1,
                    darkModeLightnessAdjustment = -48
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_container",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 2,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = -2,
                    darkModeLightnessAdjustment = -42
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_container_high", // android 14+ notification bg color
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = -4
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_container_highest",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = -5,
                    darkModeLightnessAdjustment = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_bright",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 10,
                    lightModeLightnessAdjustment = -2,
                    darkModeLightnessAdjustment = 13
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_dim", // android 14+ notification scrim color
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 1,
                    darkModeColorIndex = 11,
                    lightModeLightnessAdjustment = -9,
                    darkModeLightnessAdjustment = 7
                )
            )
            add(
                ColorMapping(
                    resourceName = "surface_variant",
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    lightModeColorIndex = 2,
                    darkModeColorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_surface_variant",
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    lightModeColorIndex = 10,
                    darkModeColorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "outline",
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    lightModeColorIndex = 7,
                    darkModeColorIndex = 6
                )
            )
            add(
                ColorMapping(
                    resourceName = "outline_variant",
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 9
                )
            )
            //            add(
            //                ColorMapping(
            //                    resourceName = "error",
            //                    lightModeColorCode = Color.parseColor("#1EB326"),
            //                    darkModeColorCode = Color.parseColor("#B5F2B8")
            //                )
            //            )
            //            add(
            //                ColorMapping(
            //                    resourceName = "on_error",
            //                    lightModeColorCode = Color.parseColor("#FFFFFF"),
            //                    darkModeColorCode = Color.parseColor("#106014")
            //                )
            //            )
            //            add(
            //                ColorMapping(
            //                    resourceName = "error_container",
            //                    lightModeColorCode = Color.parseColor("#DCF9DE"),
            //                    darkModeColorCode = Color.parseColor("#188C1D")
            //                )
            //            )
            //            add(
            //                ColorMapping(
            //                    resourceName = "on_error_container",
            //                    lightModeColorCode = Color.parseColor("#0B410E"),
            //                    darkModeColorCode = Color.parseColor("#DCF9DE")
            //                )
            //            )
            add(
                ColorMapping(
                    resourceName = "control_activated",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 8,
                    darkModeColorIndex = 4
                )
            )
            //            add(ColorMapping(resourceName = "control_normal"))
            add(
                ColorMapping(
                    resourceName = "control_highlight",
                    lightModeColorCode = Color.parseColor("#1F000000"),
                    darkModeColorCode = Color.parseColor("#33FFFFFF")
                )
            )
            //            add(ColorMapping(resourceName = "text_primary_inverse"))
            //            add(ColorMapping(resourceName = "text_secondary_and_tertiary_inverse"))
            //            add(ColorMapping(resourceName = "text_primary_inverse_disable_only"))
            //            add(ColorMapping(resourceName = "text_secondary_and_tertiary_inverse_disabled"))
            //            add(ColorMapping(resourceName = "text_hint_inverse"))
            add(
                ColorMapping(
                    resourceName = "palette_key_color_primary",
                    tonalPalette = TonalPalette.PRIMARY,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 8
                )
            )
            add(
                ColorMapping(
                    resourceName = "palette_key_color_secondary",
                    tonalPalette = TonalPalette.SECONDARY,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 8
                )
            )
            add(
                ColorMapping(
                    resourceName = "palette_key_color_tertiary",
                    tonalPalette = TonalPalette.TERTIARY,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 8
                )
            )
            add(
                ColorMapping(
                    resourceName = "palette_key_color_neutral",
                    tonalPalette = TonalPalette.NEUTRAL,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 8
                )
            )
            add(
                ColorMapping(
                    resourceName = "palette_key_color_neutral_variant",
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    lightModeColorIndex = 4,
                    darkModeColorIndex = 8
                )
            )
            // New resources which are added on Android 15 QPR1 Beta 3
            if (Build.VERSION.SDK_INT >= 35) {
                add(
                    ColorMapping(
                        resourceName = "under_surface",
                        tonalPalette = TonalPalette.NEUTRAL,
                        colorIndex = 12
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "shade_active",
                        tonalPalette = TonalPalette.PRIMARY,
                        colorIndex = 3
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "on_shade_active",
                        tonalPalette = TonalPalette.PRIMARY,
                        colorIndex = 11
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "on_shade_active_variant",
                        tonalPalette = TonalPalette.PRIMARY,
                        colorIndex = 9
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "shade_inactive",
                        tonalPalette = TonalPalette.NEUTRAL,
                        colorIndex = 10
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "on_shade_inactive",
                        tonalPalette = TonalPalette.NEUTRAL,
                        colorIndex = 3
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "on_shade_inactive_variant",
                        tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                        colorIndex = 4
                    )
                )
                add(
                    ColorMapping(
                        resourceName = "shade_disabled",
                        tonalPalette = TonalPalette.NEUTRAL,
                        colorIndex = 11
                    )
                )
                //                add(
                //                    ColorMapping(
                //                        resourceName = "overview_background",
                //                        tonalPalette = TonalPalette.NEUTRAL,
                //                        lightModeColorIndex = 5,
                //                        darkModeColorIndex = 2,
                //                    )
                //                )
            }
        }

        FIXED_COLORS_MAPPED.apply {
            add(
                ColorMapping(
                    resourceName = "primary_fixed",
                    tonalPalette = TonalPalette.PRIMARY,
                    colorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "primary_fixed_dim",
                    tonalPalette = TonalPalette.PRIMARY,
                    colorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_primary_fixed",
                    tonalPalette = TonalPalette.PRIMARY,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_primary_fixed_variant",
                    tonalPalette = TonalPalette.PRIMARY,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "secondary_fixed",
                    tonalPalette = TonalPalette.SECONDARY,
                    colorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "secondary_fixed_dim",
                    tonalPalette = TonalPalette.SECONDARY,
                    colorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_secondary_fixed",
                    tonalPalette = TonalPalette.SECONDARY,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_secondary_fixed_variant",
                    tonalPalette = TonalPalette.SECONDARY,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "tertiary_fixed",
                    tonalPalette = TonalPalette.TERTIARY,
                    colorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "tertiary_fixed_dim",
                    tonalPalette = TonalPalette.TERTIARY,
                    colorIndex = 4
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_tertiary_fixed",
                    tonalPalette = TonalPalette.TERTIARY,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "on_tertiary_fixed_variant",
                    tonalPalette = TonalPalette.TERTIARY,
                    colorIndex = 9
                )
            )
        }

        /*
         * Google uses the "gm3" prefix for these resources,
         * while MaterialComponents uses "m3" instead.
         */
        M3_REF_PALETTE.apply {
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral4",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral6",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral12",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral17",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral22",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral24",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral87",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral92",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral94",
                    lightnessAdjustment = -2,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral96",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 1
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral98",
                    lightnessAdjustment = -2,
                    tonalPalette = TonalPalette.NEUTRAL,
                    colorIndex = 1
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant4",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant6",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 11
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant12",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant17",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 10
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant22",
                    lightnessAdjustment = -26,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant24",
                    lightnessAdjustment = -9,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 9
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant87",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 3
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant92",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant94",
                    lightnessAdjustment = -2,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 2
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant96",
                    lightnessAdjustment = -4,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 1
                )
            )
            add(
                ColorMapping(
                    resourceName = "m3_ref_palette_dynamic_neutral_variant98",
                    lightnessAdjustment = -2,
                    tonalPalette = TonalPalette.NEUTRAL_VARIANT,
                    colorIndex = 1
                )
            )
        }
    }
}
