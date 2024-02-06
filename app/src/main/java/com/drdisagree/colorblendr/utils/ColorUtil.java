package com.drdisagree.colorblendr.utils;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.cam.Cam;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorUtil {

    public static @ColorInt int getColorFromAttribute(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static ArrayList<ArrayList<Integer>> generateModifiedColors(
            Context context,
            ColorSchemeUtil.MONET style,
            int accentSaturation,
            int backgroundSaturation,
            int backgroundLightness,
            boolean pitchBlackTheme,
            boolean accurateShades
    ) {
        return generateModifiedColors(
                context,
                style,
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackTheme,
                accurateShades,
                true
        );
    }

    public static ArrayList<ArrayList<Integer>> generateModifiedColors(
            Context context,
            ColorSchemeUtil.MONET style,
            int accentSaturation,
            int backgroundSaturation,
            int backgroundLightness,
            boolean pitchBlackTheme,
            boolean accurateShades,
            boolean modifyPitchBlack
    ) {
        return generateModifiedColors(
                context,
                style,
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackTheme,
                accurateShades,
                modifyPitchBlack,
                SystemUtil.isDarkMode()
        );
    }

    public static ArrayList<ArrayList<Integer>> generateModifiedColors(
            Context context,
            ColorSchemeUtil.MONET style,
            int accentSaturation,
            int backgroundSaturation,
            int backgroundLightness,
            boolean pitchBlackTheme,
            boolean accurateShades,
            boolean modifyPitchBlack,
            boolean isDark
    ) {
        String wallpaperColors = RPrefs.getString(WALLPAPER_COLOR_LIST, null);
        ArrayList<Integer> wallpaperColorList;

        if (wallpaperColors != null) {
            wallpaperColorList = Const.GSON.fromJson(
                    wallpaperColors,
                    new TypeToken<ArrayList<Integer>>() {
                    }.getType()
            );
        } else {
            wallpaperColorList = WallpaperUtil.getWallpaperColors(context);
        }

        return generateModifiedColors(
                style,
                RPrefs.getInt(
                        MONET_SEED_COLOR,
                        wallpaperColorList.get(0)
                ),
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackTheme,
                accurateShades,
                modifyPitchBlack,
                isDark,
                true
        );
    }

    public static ArrayList<ArrayList<Integer>> generateModifiedColors(
            ColorSchemeUtil.MONET style,
            @ColorInt int seedColor,
            int accentSaturation,
            int backgroundSaturation,
            int backgroundLightness,
            boolean pitchBlackTheme,
            boolean accurateShades,
            boolean modifyPitchBlack,
            boolean isDark,
            boolean overrideColors
    ) {
        ArrayList<ArrayList<Integer>> palette = ColorSchemeUtil.generateColorPalette(
                style,
                seedColor,
                isDark
        );

        // Modify colors
        for (int i = 0; i < palette.size(); i++) {
            ArrayList<Integer> modifiedShades = ColorModifiers.modifyColors(
                    new ArrayList<>(palette.get(i).subList(1, palette.get(i).size())),
                    new AtomicInteger(i),
                    style,
                    accentSaturation,
                    backgroundSaturation,
                    backgroundLightness,
                    pitchBlackTheme,
                    accurateShades,
                    modifyPitchBlack,
                    overrideColors
            );
            for (int j = 1; j < palette.get(i).size(); j++) {
                palette.get(i).set(j, modifiedShades.get(j - 1));
            }
        }

        return palette;
    }

    public static @ColorInt int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static int modifySaturation(int color, int saturation) {
        float saturationFloat = (saturation - 100) / 100f;

        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);

        if (saturationFloat > 0) {
            hsl[1] += ((1 - hsl[1]) * saturationFloat);
        } else if (saturationFloat < 0) {
            hsl[1] += (hsl[1] * saturationFloat);
        }

        return ColorUtils.HSLToColor(hsl);
    }

    public static int modifyLightness(int color, int lightness, int idx) {
        float lightnessFloat = (lightness - 100) / 1000f;
        float shade = getSystemTintList()[idx];

        if (idx == 0 || idx == 12) {
            lightnessFloat = 0;
        } else if (idx == 1) {
            lightnessFloat /= 10;
        } else if (idx == 2) {
            lightnessFloat /= 2;
        }

        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);

        hsl[2] = shade + lightnessFloat;

        return ColorUtils.HSLToColor(hsl);
    }

    public static float getHue(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);

        return hsl[0];
    }

    public static float[] getSystemTintList() {
        return new float[]{1.0f, 0.99f, 0.95f, 0.9f, 0.8f, 0.7f, 0.6f, 0.496f, 0.4f, 0.3f, 0.2f, 0.1f, 0.0f};
    }

    public static String[][] getColorNames() {
        String[] accentTypes = {"system_accent1", "system_accent2", "system_accent3", "system_neutral1", "system_neutral2"};
        String[] values = {"0", "10", "50", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"};

        String[][] colorNames = new String[accentTypes.length][values.length];

        for (int i = 0; i < accentTypes.length; i++) {
            for (int j = 0; j < values.length; j++) {
                colorNames[i][j] = accentTypes[i] + "_" + values[j];
            }
        }

        return colorNames;
    }

    public static String[][] getColorNamesM3(boolean isDynamic, boolean prefixG) {
        String prefix = "m3_ref_palette_";
        String dynamic = "dynamic_";

        String[] accentTypes = {"primary", "secondary", "tertiary", "neutral", "neutral_variant"};
        String[] values = {"100", "99", "95", "90", "80", "70", "60", "50", "40", "30", "20", "10", "0"};

        String[][] colorNames = new String[accentTypes.length][values.length];

        for (int i = 0; i < accentTypes.length; i++) {
            for (int j = 0; j < values.length; j++) {
                colorNames[i][j] = (prefixG ? "g" : "") + prefix + (isDynamic ? dynamic : "") + accentTypes[i] + values[j];
            }
        }

        return colorNames;
    }

    public static String intToHexColor(int colorInt) {
        return String.format("#%06X", (0xFFFFFF & colorInt));
    }

    public static String intToHexColorNoHash(int colorInt) {
        return String.format("%06X", (0xFFFFFF & colorInt));
    }

    public static int[][] getSystemColors(Context context) {
        return new int[][]{
                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_primary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_secondary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_tertiary0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral0, context.getTheme())
                },

                new int[]{
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant100, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant99, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant95, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant90, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant80, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant70, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant60, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant50, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant40, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant30, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant20, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant10, context.getTheme()),
                        context.getResources().getColor(com.google.android.material.R.color.material_dynamic_neutral_variant0, context.getTheme())
                }};
    }

    public static int calculateTextColor(@ColorInt int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;

        return darkness < 0.5 ? Color.BLACK : Color.WHITE;
    }

    /**
     * Convert a color appearance model representation to an ARGB color.
     * <p>
     * Note: the returned color may have a lower chroma than requested. Whether a chroma is
     * available depends on luminance. For example, there's no such thing as a high chroma light
     * red, due to the limitations of our eyes and/or physics. If the requested chroma is
     * unavailable, the highest possible chroma at the requested luminance is returned.
     *
     * @param hue    hue, in degrees, in CAM coordinates
     * @param chroma chroma in CAM coordinates.
     * @param lstar  perceptual luminance, L* in L*a*b*
     */
    @ColorInt
    public static int CAMToColor(float hue, float chroma, float lstar) {
        return Cam.getInt(hue, chroma, lstar);
    }

    private static final double XYZ_WHITE_REFERENCE_X = 95.047;
    private static final double XYZ_WHITE_REFERENCE_Y = 100;
    private static final double XYZ_WHITE_REFERENCE_Z = 108.883;

    /**
     * Converts a color from CIE XYZ to its RGB representation.
     *
     * <p>This method expects the XYZ representation to use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).</p>
     *
     * @param x X component value [0...95.047)
     * @param y Y component value [0...100)
     * @param z Z component value [0...108.883)
     * @return int containing the RGB representation
     */
    @ColorInt
    public static int XYZToColor(@FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_X) double x,
                                 @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Y) double y,
                                 @FloatRange(from = 0f, to = XYZ_WHITE_REFERENCE_Z) double z) {
        double r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100;
        double g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100;
        double b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100;

        r = r > 0.0031308 ? 1.055 * Math.pow(r, 1 / 2.4) - 0.055 : 12.92 * r;
        g = g > 0.0031308 ? 1.055 * Math.pow(g, 1 / 2.4) - 0.055 : 12.92 * g;
        b = b > 0.0031308 ? 1.055 * Math.pow(b, 1 / 2.4) - 0.055 : 12.92 * b;

        return Color.rgb(
                constrain((int) Math.round(r * 255), 0, 255),
                constrain((int) Math.round(g * 255), 0, 255),
                constrain((int) Math.round(b * 255), 0, 255));
    }

    private static float constrain(float amount, float low, float high) {
        return amount < low ? low : Math.min(amount, high);
    }

    @SuppressWarnings("SameParameterValue")
    private static int constrain(int amount, int low, int high) {
        return amount < low ? low : Math.min(amount, high);
    }

    public static ArrayList<Integer> getMonetAccentColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(ColorBlendr.getAppContext(), android.R.color.system_accent1_400));
        colors.add(ContextCompat.getColor(ColorBlendr.getAppContext(), android.R.color.system_accent2_400));
        colors.add(ContextCompat.getColor(ColorBlendr.getAppContext(), android.R.color.system_accent3_400));
        return colors;
    }
}
