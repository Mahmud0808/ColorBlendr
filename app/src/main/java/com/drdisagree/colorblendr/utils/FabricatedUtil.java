package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;

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
}
