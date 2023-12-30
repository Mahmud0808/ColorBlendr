package com.drdisagree.colorblendr.utils;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.monet.hct.Hct;
import com.drdisagree.colorblendr.utils.monet.palettes.TonalPalette;
import com.drdisagree.colorblendr.utils.monet.scheme.DynamicScheme;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeContent;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeExpressive;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeFidelity;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeFruitSalad;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeMonochrome;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeNeutral;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeRainbow;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeTonalSpot;
import com.drdisagree.colorblendr.utils.monet.scheme.SchemeVibrant;

import java.util.ArrayList;

public class ColorSchemeUtil {

    public enum MONET {
        SPRITZ,
        MONOCHROMATIC,
        TONAL_SPOT,
        VIBRANT,
        RAINBOW,
        EXPRESSIVE,
        FIDELITY,
        CONTENT,
        FRUIT_SALAD
    }

    public static final int[] tones = {100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0};

    public static ArrayList<ArrayList<Integer>> generateColorPalette(MONET style, @ColorInt int color) {
        return generateColorPalette(style, color, SystemUtil.isDarkMode(), 5);
    }

    public static ArrayList<ArrayList<Integer>> generateColorPalette(MONET style, @ColorInt int color, boolean isDark) {
        return generateColorPalette(style, color, isDark, 5);
    }

    public static ArrayList<ArrayList<Integer>> generateColorPalette(MONET style, @ColorInt int color, boolean isDark, int contrast) {
        ArrayList<ArrayList<Integer>> palette = new ArrayList<>();

        DynamicScheme dynamicScheme = getDynamicScheme(style, color, isDark, contrast);

        TonalPalette[] tonalPalettes = {
                dynamicScheme.primaryPalette,
                dynamicScheme.secondaryPalette,
                dynamicScheme.tertiaryPalette,
                dynamicScheme.neutralPalette,
                dynamicScheme.neutralVariantPalette
        };

        for (TonalPalette tonalPalette : tonalPalettes) {
            palette.add(createToneList(tonalPalette));
        }

        return palette;
    }

    public static DynamicScheme getDynamicScheme(MONET style, @ColorInt int color, boolean isDark, int contrast) {
        return switch (style) {
            case SPRITZ -> new SchemeNeutral(Hct.fromInt(color), isDark, contrast);
            case MONOCHROMATIC -> new SchemeMonochrome(Hct.fromInt(color), isDark, contrast);
            case TONAL_SPOT -> new SchemeTonalSpot(Hct.fromInt(color), isDark, contrast);
            case VIBRANT -> new SchemeVibrant(Hct.fromInt(color), isDark, contrast);
            case RAINBOW -> new SchemeRainbow(Hct.fromInt(color), isDark, contrast);
            case EXPRESSIVE -> new SchemeExpressive(Hct.fromInt(color), isDark, contrast);
            case FIDELITY -> new SchemeFidelity(Hct.fromInt(color), isDark, contrast);
            case CONTENT -> new SchemeContent(Hct.fromInt(color), isDark, contrast);
            case FRUIT_SALAD -> new SchemeFruitSalad(Hct.fromInt(color), isDark, contrast);
        };
    }

    private static ArrayList<Integer> createToneList(TonalPalette palette) {
        ArrayList<Integer> toneList = new ArrayList<>();
        for (int tone : ColorSchemeUtil.tones) {
            toneList.add(palette.tone(tone));
        }
        return toneList;
    }

    public static MONET stringToEnumMonetStyle(Context context, String enumString) {
        if (enumString.equals(context.getString(R.string.monet_neutral))) {
            return MONET.SPRITZ;
        } else if (enumString.equals(context.getString(R.string.monet_monochrome))) {
            return MONET.MONOCHROMATIC;
        } else if (enumString.equals(context.getString(R.string.monet_tonalspot))) {
            return MONET.TONAL_SPOT;
        } else if (enumString.equals(context.getString(R.string.monet_vibrant))) {
            return MONET.VIBRANT;
        } else if (enumString.equals(context.getString(R.string.monet_rainbow))) {
            return MONET.RAINBOW;
        } else if (enumString.equals(context.getString(R.string.monet_expressive))) {
            return MONET.EXPRESSIVE;
        } else if (enumString.equals(context.getString(R.string.monet_fidelity))) {
            return MONET.FIDELITY;
        } else if (enumString.equals(context.getString(R.string.monet_content))) {
            return MONET.CONTENT;
        } else if (enumString.equals(context.getString(R.string.monet_fruitsalad))) {
            return MONET.FRUIT_SALAD;
        } else {
            return MONET.TONAL_SPOT;
        }
    }
}
