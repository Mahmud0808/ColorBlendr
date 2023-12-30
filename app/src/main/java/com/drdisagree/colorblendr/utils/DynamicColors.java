package com.drdisagree.colorblendr.utils;

import androidx.core.util.Pair;

import com.drdisagree.colorblendr.utils.monet.dynamiccolor.DynamicColor;
import com.drdisagree.colorblendr.utils.monet.dynamiccolor.MaterialDynamicColors;

import java.util.ArrayList;
import java.util.List;

public class DynamicColors {

    private static final MaterialDynamicColors MDC = new MaterialDynamicColors();
    public static final List<Pair<String, DynamicColor>> ALL_DYNAMIC_COLORS_MAPPED = new ArrayList<>();
    public static final List<Pair<String, Pair<Integer, Integer>>> FIXED_COLORS_MAPPED = new ArrayList<>();
    public static final List<Pair<String, Pair<Integer, Pair<Integer, Integer>>>> M3_REF_PALETTE = new ArrayList<>();

    /*
     * This is a list of all the dynamic and fixed colors that are available in the system.
     *
     * Fixed colors:
     * First item of inner pair is the tonal palette index.
     * 0 = primary
     * 1 = secondary
     * 2 = tertiary
     * 3 = neutral
     * 4 = neutral variant
     * Second item of the inner pair is the color index.
     *
     * M3 ref colors:
     * First item of inner pair is the lightness percentage to increase or decrease
     * from the base color given by the second item of the inner pair.
     *
     * First item of the inner-inner pair is the tonal palette index.
     * 0 = primary
     * 1 = secondary
     * 2 = tertiary
     * 3 = neutral
     * 4 = neutral variant
     * Second item of the inner-inner pair is the color index.
     */
    static {
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("primary_container", MDC.primaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_primary_container", MDC.onPrimaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("primary", MDC.primary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_primary", MDC.onPrimary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("secondary_container", MDC.secondaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_secondary_container", MDC.onSecondaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("secondary", MDC.secondary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_secondary", MDC.onSecondary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("tertiary_container", MDC.tertiaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_tertiary_container", MDC.onTertiaryContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("tertiary", MDC.tertiary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_tertiary", MDC.onTertiary()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("background", MDC.background()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_background", MDC.onBackground()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface", MDC.surface()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_surface", MDC.onSurface()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_low", MDC.surfaceContainerLow()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_lowest", MDC.surfaceContainerLowest()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container", MDC.surfaceContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_high", MDC.surfaceContainerHigh()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_container_highest", MDC.surfaceContainerHighest()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_bright", MDC.surfaceBright()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_dim", MDC.surfaceDim()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("surface_variant", MDC.surfaceVariant()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_surface_variant", MDC.onSurfaceVariant()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("outline", MDC.outline()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("outline_variant", MDC.outlineVariant()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("error", MDC.error()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_error", MDC.onError()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("error_container", MDC.errorContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("on_error_container", MDC.onErrorContainer()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_activated", MDC.controlActivated()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_normal", MDC.controlNormal()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("control_highlight", MDC.controlHighlight()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_primary_inverse", MDC.textPrimaryInverse()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_secondary_and_tertiary_inverse", MDC.textSecondaryAndTertiaryInverse()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_primary_inverse_disable_only", MDC.textPrimaryInverseDisableOnly()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_secondary_and_tertiary_inverse_disabled", MDC.textSecondaryAndTertiaryInverseDisabled()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("text_hint_inverse", MDC.textHintInverse()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_primary", MDC.primaryPaletteKeyColor()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_secondary", MDC.secondaryPaletteKeyColor()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_tertiary", MDC.tertiaryPaletteKeyColor()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_neutral", MDC.neutralPaletteKeyColor()));
        ALL_DYNAMIC_COLORS_MAPPED.add(Pair.create("palette_key_color_neutral_variant", MDC.neutralVariantPaletteKeyColor()));

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
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral4", Pair.create(-6, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral6", Pair.create(-4, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral12", Pair.create(2, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral17", Pair.create(7, Pair.create(3, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral22", Pair.create(2, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral24", Pair.create(4, Pair.create(3, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral87", Pair.create(7, Pair.create(3, 4))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral92", Pair.create(2, Pair.create(3, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral94", Pair.create(4, Pair.create(3, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral96", Pair.create(1, Pair.create(3, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral98", Pair.create(3, Pair.create(3, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant4", Pair.create(-6, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant6", Pair.create(-4, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant12", Pair.create(2, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant17", Pair.create(7, Pair.create(4, 11))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant22", Pair.create(2, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant24", Pair.create(4, Pair.create(4, 10))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant87", Pair.create(7, Pair.create(4, 4))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant92", Pair.create(2, Pair.create(4, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant94", Pair.create(4, Pair.create(4, 3))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant96", Pair.create(1, Pair.create(4, 2))));
        M3_REF_PALETTE.add(Pair.create("m3_ref_palette_dynamic_neutral_variant98", Pair.create(3, Pair.create(4, 2))));
    }
}
