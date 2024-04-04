package com.drdisagree.colorblendr.utils;

import android.os.Build;

import androidx.core.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class DynamicColors {

    public static final List<Pair<String, Object>> ALL_DYNAMIC_COLORS_MAPPED = new ArrayList<>();
    public static final List<Pair<String, Pair<Integer, Integer>>> FIXED_COLORS_MAPPED = new ArrayList<>();
    public static final List<Pair<String, Pair<Integer, Pair<Integer, Integer>>>> M3_REF_PALETTE = new ArrayList<>();

    /*
     * This is a list of all the dynamic and fixed colors that are available in the system.
     *
     * **Dynamic colors:**
     * -> First item is the name of the color.
     * -> Second item is the tonal palette index.
     * 0 = primary
     * 1 = secondary
     * 2 = tertiary
     * 3 = neutral
     * 4 = neutral variant
     * -> Third item is the color index in light mode.
     * -> Fourth item is the color index in dark mode.
     *
     * **Fixed colors:**
     * -> First item is the name of the color.
     * -> Second item is the tonal palette index.
     * 0 = primary
     * 1 = secondary
     * 2 = tertiary
     * 3 = neutral
     * 4 = neutral variant
     * -> Third item is the color index.
     *
     * **M3 ref colors:**
     * -> First item is the name of the color.
     * Second item is the lightness percentage to increase or decrease
     * from the base color given by the second item of the inner pair.
     * -> Third item is the tonal palette index.
     * 0 = primary
     * 1 = secondary
     * 2 = tertiary
     * 3 = neutral
     * 4 = neutral variant
     * -> Fourth item is the color index.
     */
    static {
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("primary_container", Pair.create(0, Pair.create(3, 9))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_primary_container", Pair.create(0, Pair.create(11, 3))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("primary", Pair.create(0, Pair.create(8, 4))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_primary", Pair.create(0, Pair.create(0, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("secondary_container", Pair.create(1, Pair.create(3, 9))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_secondary_container", Pair.create(1, Pair.create(11, 3))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("secondary", Pair.create(1, Pair.create(8, 4))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_secondary", Pair.create(1, Pair.create(0, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("tertiary_container", Pair.create(2, Pair.create(3, 9))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_tertiary_container", Pair.create(2, Pair.create(11, 3))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("tertiary", Pair.create(2, Pair.create(8, 4))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_tertiary", Pair.create(2, Pair.create(0, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("background", Pair.create(3, Pair.create(1, 11))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_background", Pair.create(3, Pair.create(11, 1))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface", Pair.create(3, Pair.create(1, 11))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_surface", Pair.create(3, Pair.create(10, 2))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_low", Pair.create(3, Pair.create(2, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_lowest", Pair.create(3, Pair.create(1, 11))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container", Pair.create(3, Pair.create(2, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_high", Pair.create(3, Pair.create(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ? 1 : 2, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_highest", Pair.create(3, Pair.create(3, 9))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_bright", Pair.create(3, Pair.create(2, 11))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_dim", Pair.create(3, Pair.create(2, 11))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_variant", Pair.create(4, Pair.create(2, 10))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_surface_variant", Pair.create(4, Pair.create(10, 2))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("outline", Pair.create(4, Pair.create(7, 6))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("outline_variant", Pair.create(4, Pair.create(4, 9))));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("error", Pair.create("#1EB326", "#B5F2B8")));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_error", Pair.create("#FFFFFF", "#106014")));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("error_container", Pair.create("#DCF9DE", "#188C1D")));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_error_container", Pair.create("#0B410E", "#DCF9DE")));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_activated", Pair.create(1, Pair.create(8, 4))));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_normal", ));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_highlight", Pair.create("#1F000000", "#33FFFFFF")));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_primary_inverse", ));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_secondary_and_tertiary_inverse", ));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_primary_inverse_disable_only", ));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_secondary_and_tertiary_inverse_disabled", ));
//        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_hint_inverse", ));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_primary", Pair.create(0, Pair.create(4, 8))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_secondary", Pair.create(1, Pair.create(4, 8))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_tertiary", Pair.create(2, Pair.create(4, 8))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_neutral", Pair.create(3, Pair.create(4, 8))));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_neutral_variant", Pair.create(4, Pair.create(4, 8))));

        FIXED_COLORS_MAPPED.add(Pair.create("primary_fixed", Pair.create(0, 3)));
        FIXED_COLORS_MAPPED.add(Pair.create("primary_fixed_dim", Pair.create(0, 4)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_primary_fixed", Pair.create(0, 11)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_primary_fixed_variant", Pair.create(0, 9)));
        FIXED_COLORS_MAPPED.add(Pair.create("secondary_fixed", Pair.create(1, 3)));
        FIXED_COLORS_MAPPED.add(Pair.create("secondary_fixed_dim", Pair.create(1, 4)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_secondary_fixed", Pair.create(1, 11)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_secondary_fixed_variant", Pair.create(1, 9)));
        FIXED_COLORS_MAPPED.add(Pair.create("tertiary_fixed", Pair.create(2, 4)));
        FIXED_COLORS_MAPPED.add(Pair.create("tertiary_fixed_dim", Pair.create(2, 4)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_tertiary_fixed", Pair.create(2, 11)));
        FIXED_COLORS_MAPPED.add(Pair.create("on_tertiary_fixed_variant", Pair.create(2, 9)));

        // Google uses "gm3" prefix for these resources, MaterialComponents use "m3" instead.
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral4", Pair.create(-60, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral6", Pair.create(-40, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral12", Pair.create(20, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral17", Pair.create(70, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral22", Pair.create(20, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral24", Pair.create(40, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral87", Pair.create(70, Pair.create(3, 4))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral92", Pair.create(20, Pair.create(3, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral94", Pair.create(40, Pair.create(3, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral96", Pair.create(10, Pair.create(3, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral98", Pair.create(30, Pair.create(3, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant4", Pair.create(-60, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant6", Pair.create(-40, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant12", Pair.create(20, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant17", Pair.create(70, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant22", Pair.create(20, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant24", Pair.create(40, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant87", Pair.create(70, Pair.create(4, 4))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant92", Pair.create(20, Pair.create(4, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant94", Pair.create(40, Pair.create(4, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant96", Pair.create(10, Pair.create(4, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant98", Pair.create(30, Pair.create(4, 2))));
    }
}
