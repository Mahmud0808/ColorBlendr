package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FabricatedUtil {

    private static final String[][] colorNames = ColorUtil.getColorNames();
    private static final String[][] colorNamesM3var1 = ColorUtil.getColorNamesM3(false, false);
    private static final String[][] colorNamesM3var2 = ColorUtil.getColorNamesM3(true, false);
    private static final String[][] colorNamesM3var3 = ColorUtil.getColorNamesM3(true, true);
    private static final String[][] colorNamesM3var4 = ColorUtil.getColorNamesM3(false, true);

    public static void createDynamicOverlay(
            FabricatedOverlayResource overlay,
            ArrayList<ArrayList<Integer>> paletteLight,
            ArrayList<ArrayList<Integer>> paletteDark
    ) {
        assignDynamicPaletteToOverlay(overlay, true /* isDark */, paletteDark);
        assignDynamicPaletteToOverlay(overlay, false /* isDark */, paletteLight);
        assignFixedColorsToOverlay(overlay, paletteLight);
    }

    @SuppressWarnings("unchecked")
    private static void assignDynamicPaletteToOverlay(FabricatedOverlayResource overlay, boolean isDark, ArrayList<ArrayList<Integer>> palette) {
        String suffix = isDark ? "dark" : "light";
        boolean pitchBlackTheme = RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);

        DynamicColors.ALL_DYNAMIC_COLORS_MAPPED.forEach(pair -> {
            String resourceName = "system_" + pair.first + "_" + suffix;
            int colorValue;

            Pair<Object, Object> valPair = (Pair<Object, Object>) pair.second;
            if (valPair.first instanceof String) {
                colorValue = Color.parseColor((String) (isDark ?
                        valPair.second :
                        valPair.first
                ));
            } else {
                Pair<Integer, Integer> colorIndexPair = (Pair<Integer, Integer>) valPair.second;
                colorValue = palette.get((Integer) valPair.first).get(
                        isDark ?
                                colorIndexPair.second :
                                colorIndexPair.first
                );

                colorValue = replaceColorForPitchBlackTheme(
                        pitchBlackTheme,
                        resourceName,
                        colorValue,
                        isDark ?
                                colorIndexPair.second :
                                colorIndexPair.first
                );
            }

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
        boolean pitchBlackTheme = RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);

        // Format of pair: <resourceName, <lightnessToChange, <colorIndexRow, colorIndexColumn>>>
        DynamicColors.M3_REF_PALETTE.forEach(pair -> {
            String resourceName = pair.first;

            Pair<Integer, Pair<Integer, Integer>> valPair = pair.second;

            // TODO: Use lightness value to modify the color
            // int lightnessToChange = valPair.first + 100;

            Pair<Integer, Integer> colorIndexPair = valPair.second;
            int baseColor = palette.get(colorIndexPair.first).get(colorIndexPair.second);
            baseColor = replaceColorForPitchBlackTheme(pitchBlackTheme, resourceName, baseColor, colorIndexPair.second);

            overlay.setColor(resourceName, baseColor);
            overlay.setColor("g" + resourceName, baseColor);
        });

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 13; j++) {
                overlay.setColor(colorNamesM3var1[i][j], palette.get(i).get(j));
                overlay.setColor(colorNamesM3var2[i][j], palette.get(i).get(j));
                overlay.setColor(colorNamesM3var3[i][j], palette.get(i).get(j));
                overlay.setColor(colorNamesM3var4[i][j], palette.get(i).get(j));
            }
        }

        replaceColorsPerPackageName(overlay, palette, pitchBlackTheme);

        if (!RPrefs.getBoolean(TINT_TEXT_COLOR, true)) {
            addTintlessTextColors(overlay);
        }
    }

    public static void getAndSaveSelectedFabricatedApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        HashMap<String, Boolean> selectedApps = new HashMap<>();
//        selectedApps.put(BuildConfig.APPLICATION_ID, true);

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
            int lightness = RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100);
            return switch (resourceName) {
                case "m3_ref_palette_dynamic_neutral_variant6",
                        "gm3_ref_palette_dynamic_neutral_variant6",
                        "system_background_dark",
                        "system_surface_dark" -> Color.BLACK;
                case "m3_ref_palette_dynamic_neutral_variant12",
                        "gm3_ref_palette_dynamic_neutral_variant12" ->
                        ColorUtil.modifyLightness(colorValue, lightness - 40, colorIndex);
                case "m3_ref_palette_dynamic_neutral_variant17",
                        "gm3_ref_palette_dynamic_neutral_variant17",
                        "gm3_system_bar_color_night" ->
                        ColorUtil.modifyLightness(colorValue, lightness - 60, colorIndex);
                case "system_surface_container_dark" ->
                        ColorUtil.modifyLightness(colorValue, lightness - 20, colorIndex);
                default -> colorValue;
            };
        }

        return colorValue;
    }

    public static void addTintlessTextColors(FabricatedOverlayResource overlay) {
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

        // Dark mode
        ArrayList<Pair<String, Integer>> resourcesDark = new ArrayList<>();
        resourcesDark.add(new Pair<>("m3_ref_palette_dynamic_neutral90", Color.WHITE));
        resourcesDark.add(new Pair<>("m3_ref_palette_dynamic_neutral95", Color.WHITE));
        resourcesDark.add(new Pair<>("m3_ref_palette_dynamic_neutral_variant70", 0xFFCCCCCC));
        resourcesDark.add(new Pair<>("m3_ref_palette_dynamic_neutral_variant80", Color.WHITE));
        resourcesDark.add(new Pair<>("text_color_primary_dark", Color.WHITE));
        resourcesDark.add(new Pair<>("text_color_secondary_dark", 0xB3FFFFFF));
        resourcesDark.add(new Pair<>("text_color_tertiary_dark", 0x80FFFFFF));
        resourcesDark.add(new Pair<>("google_dark_default_color_on_background", Color.WHITE));
        resourcesDark.add(new Pair<>("gm_ref_palette_grey500", Color.WHITE));

        // Light mode
        ArrayList<Pair<String, Integer>> resourcesLight = new ArrayList<>();
        resourcesLight.add(new Pair<>("m3_ref_palette_dynamic_neutral10", Color.BLACK));
        resourcesLight.add(new Pair<>("m3_ref_palette_dynamic_neutral_variant30", 0xB3000000));
        resourcesLight.add(new Pair<>("text_color_primary_light", Color.BLACK));
        resourcesLight.add(new Pair<>("text_color_secondary_light", 0xB3000000));
        resourcesLight.add(new Pair<>("text_color_tertiary_light", 0x80000000));
        resourcesDark.add(new Pair<>("google_default_color_on_background", Color.BLACK));
        resourcesDark.add(new Pair<>("gm_ref_palette_grey700", Color.BLACK));

        for (Pair<String, Integer> pair : resourcesDark) {
            overlay.setColor(pair.first, pair.second);
            overlay.setColor("g" + pair.first, pair.second);
        }

        for (Pair<String, Integer> pair : resourcesLight) {
            overlay.setColor(pair.first, pair.second);
            overlay.setColor("g" + pair.first, pair.second);
        }

        // For settings text color on android 14
        overlay.setColor("settingslib_text_color_primary_device_default", Color.WHITE, "night");
        overlay.setColor("settingslib_text_color_secondary_device_default", 0xB3FFFFFF, "night");
    }

    private static void replaceColorsPerPackageName(FabricatedOverlayResource overlay, ArrayList<ArrayList<Integer>> palette, boolean pitchBlackTheme) {
        if (overlay.targetPackage.startsWith("com.android.systemui.clocks.")) { // Android 14 clocks
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 13; j++) {
                    overlay.setColor(colorNames[i][j], palette.get(i).get(j));
                }
            }
        } else if (overlay.targetPackage.equals("com.google.android.googlequicksearchbox")) { // Google Feeds
            if (pitchBlackTheme) {
                overlay.setColor("gm3_ref_palette_dynamic_neutral_variant20", Color.BLACK);
            }
        } else if (overlay.targetPackage.equals("com.google.android.apps.magazines")) { // Google News
            overlay.setColor("cluster_divider_bg", Color.TRANSPARENT);
            overlay.setColor("cluster_divider_border", Color.TRANSPARENT);

            overlay.setColor("appwidget_background_day", palette.get(3).get(2));
            overlay.setColor("home_background_day", palette.get(3).get(2));
            overlay.setColor("google_default_color_background", palette.get(3).get(2));
            overlay.setColor("gm3_system_bar_color_day", palette.get(4).get(3));
            overlay.setColor("google_default_color_on_background", palette.get(3).get(11));
            overlay.setColor("google_dark_default_color_on_background", palette.get(3).get(1));
            overlay.setColor("google_default_color_on_background", palette.get(3).get(11));
            overlay.setColor("gm3_system_bar_color_night", palette.get(4).get(10));

            if (pitchBlackTheme) {
                overlay.setColor("appwidget_background_night", Color.BLACK);
                overlay.setColor("home_background_night", Color.BLACK);
                overlay.setColor("google_dark_default_color_background", Color.BLACK);
            } else {
                overlay.setColor("appwidget_background_night", palette.get(3).get(11));
                overlay.setColor("home_background_night", palette.get(3).get(11));
                overlay.setColor("google_dark_default_color_background", palette.get(3).get(11));
            }
        } else if (overlay.targetPackage.equals("com.google.android.play.games")) { // Google Play Games
            // Light mode
            overlay.setColor("google_white", palette.get(3).get(2));
            overlay.setColor("gm_ref_palette_grey300", palette.get(4).get(4));
            overlay.setColor("gm_ref_palette_grey700", palette.get(3).get(11));
            overlay.setColor("replay__pal_games_light_600", palette.get(0).get(8));

            // Dark mode
            overlay.setColor("gm_ref_palette_grey900", pitchBlackTheme ? Color.BLACK : palette.get(3).get(11));
            overlay.setColor("gm_ref_palette_grey600", palette.get(4).get(8));
            overlay.setColor("gm_ref_palette_grey500", palette.get(3).get(1));
            overlay.setColor("replay__pal_games_dark_300", palette.get(0).get(5));
        }
    }
}
