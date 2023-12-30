package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.TINT_TEXT_COLOR;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.core.util.Pair;

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource;
import com.drdisagree.colorblendr.utils.monet.scheme.DynamicScheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FabricatedUtil {

    public static void createDynamicOverlay(
            FabricatedOverlayResource overlay,
            ArrayList<ArrayList<Integer>> paletteLight,
            DynamicScheme mDynamicSchemeDark,
            DynamicScheme mDynamicSchemeLight
    ) {
        assignDynamicPaletteToOverlay(overlay, true /* isDark */, mDynamicSchemeDark);
        assignDynamicPaletteToOverlay(overlay, false /* isDark */, mDynamicSchemeLight);
        assignFixedColorsToOverlay(overlay, paletteLight);
    }

    private static void assignDynamicPaletteToOverlay(FabricatedOverlayResource overlay, boolean isDark, DynamicScheme mDynamicScheme) {
        String suffix = isDark ? "dark" : "light";
        DynamicColors.ALL_DYNAMIC_COLORS_MAPPED.forEach(pair -> {
            String resourceName = "system_" + pair.first + "_" + suffix;
            int colorValue = pair.second.getArgb(mDynamicScheme);
            overlay.setColor(resourceName, colorValue);
        });
    }

    private static void assignFixedColorsToOverlay(FabricatedOverlayResource overlay, ArrayList<ArrayList<Integer>> paletteLight) {
        DynamicColors.FIXED_COLORS_MAPPED.forEach(pair -> {
            String resourceName = "system_" + pair.first;
            int colorValue = paletteLight.get(pair.second.first).get(pair.second.second);
            overlay.setColor(resourceName, colorValue);
        });
    }

    public static void assignPerAppColorsToOverlay(FabricatedOverlayResource overlay, ArrayList<ArrayList<Integer>> palette) {
        DynamicColors.M3_REF_PALETTE.forEach(pair -> {
            // Format of pair: <resourceName, <lightnessToChange, <colorIndexRow, colorIndexColumn>>>
            boolean pitchBlackTheme = RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);

            String resourceName = pair.first;

            Pair<Integer, Pair<Integer, Integer>> valPair = pair.second;
            int lightnessToChange = valPair.first + 100;

            Pair<Integer, Integer> colorIndexPair = valPair.second;
            int baseColor = palette.get(colorIndexPair.first).get(colorIndexPair.second);

            int colorValue = ColorUtil.modifyLightness(baseColor, lightnessToChange, colorIndexPair.second);
            colorValue = replaceColorForPitchBlackTheme(pitchBlackTheme, resourceName, colorValue, colorIndexPair.second);

            overlay.setColor(resourceName, colorValue);
            overlay.setColor("g" + resourceName, colorValue);
        });

        if (!RPrefs.getBoolean(TINT_TEXT_COLOR, true)) {
            addUnpaintedTextColors(overlay.targetPackage, overlay);
        }
    }

    public static void getAndSaveSelectedFabricatedApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        HashMap<String, Boolean> selectedApps = new HashMap<>();

        for (ApplicationInfo appInfo : applications) {
            String packageName = appInfo.packageName;
            boolean isSelected = OverlayManager.isOverlayEnabled(
                    String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
            );

            if (isSelected) {
                selectedApps.put(packageName, true);
            }
        }

        Const.saveSelectedFabricatedApps(selectedApps);
    }

    public static @ColorInt int replaceColorForPitchBlackTheme(boolean pitchBlackTheme, String resourceName, int colorValue, int colorIndex) {
        if (pitchBlackTheme) {
            return switch (resourceName) {
                case "m3_ref_palette_dynamic_neutral_variant6" -> Color.BLACK;
                case "m3_ref_palette_dynamic_neutral_variant12" ->
                        ColorUtil.modifyLightness(colorValue, 40, colorIndex);
                case "m3_ref_palette_dynamic_neutral_variant17" ->
                        ColorUtil.modifyLightness(colorValue, 60, colorIndex);
                default -> colorValue;
            };
        }

        return colorValue;
    }

    public static void addUnpaintedTextColors(String packageName, FabricatedOverlayResource overlay) {
        String[] prefixes = new String[]{
                "m3_sys_color_",
                "m3_sys_color_dynamic_"
        };
        String[] variants = new String[]{
                "dark_",
                "light_",
        };
        String[] suffixes = new String[]{
                "on_surface",
                "on_surface_variant",
                "on_background"
        };

        for (String prefix : prefixes) {
            for (String variant : variants) {
                for (String suffix : suffixes) {
                    String resourceName = prefix + variant + suffix;
                    overlay.setColor(
                            resourceName,
                            variant.contains("dark") ?
                                    Color.WHITE :
                                    Color.BLACK
                    );
                }
            }
        }

        if (packageName.equals("com.google.android.gm")) {
            overlay.setColor("m3_ref_palette_dynamic_neutral90", Color.WHITE);
            overlay.setColor("gm3_ref_palette_dynamic_neutral90", Color.WHITE);
            overlay.setColor("m3_ref_palette_dynamic_neutral_variant70", 0xB3FFFFFF);
            overlay.setColor("gm3_ref_palette_dynamic_neutral_variant70", 0xB3FFFFFF);
            overlay.setColor("m3_ref_palette_dynamic_neutral10", Color.BLACK);
            overlay.setColor("gm3_ref_palette_dynamic_neutral10", Color.BLACK);
            overlay.setColor("m3_ref_palette_dynamic_neutral_variant30", 0xB3000000);
            overlay.setColor("gm3_ref_palette_dynamic_neutral_variant30", 0xB3000000);
        }
    }
}
