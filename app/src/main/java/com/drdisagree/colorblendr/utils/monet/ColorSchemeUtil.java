package com.drdisagree.colorblendr.utils.monet;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.SystemUtil;
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
        NEUTRAL,
        MONOCHROME,
        TONALSPOT,
        VIBRANT,
        RAINBOW,
        EXPRESSIVE,
        FIDELITY,
        CONTENT,
        FRUIT_SALAD
    }

    public static final int[] tones = {100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0};

    public static ArrayList<ArrayList<Integer>> generateColorPalette(MONET style, @ColorInt int color) {
        ArrayList<ArrayList<Integer>> palette = new ArrayList<>();

        DynamicScheme dynamicScheme = switch (style) {
            case NEUTRAL -> new SchemeNeutral(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case MONOCHROME -> new SchemeMonochrome(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case TONALSPOT -> new SchemeTonalSpot(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case VIBRANT -> new SchemeVibrant(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case RAINBOW -> new SchemeRainbow(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case EXPRESSIVE -> new SchemeExpressive(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case FIDELITY -> new SchemeFidelity(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case CONTENT -> new SchemeContent(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
            case FRUIT_SALAD ->
                    new SchemeFruitSalad(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);
        };

        TonalPalette[] palettes = {
                dynamicScheme.primaryPalette,
                dynamicScheme.secondaryPalette,
                dynamicScheme.tertiaryPalette,
                dynamicScheme.neutralPalette,
                dynamicScheme.neutralVariantPalette
        };

        for (TonalPalette paletteType : palettes) {
            palette.add(createToneList(paletteType));
        }

        return palette;
    }

    private static ArrayList<Integer> createToneList(TonalPalette palette) {
        ArrayList<Integer> toneList = new ArrayList<>();
        for (int tone : ColorSchemeUtil.tones) {
            toneList.add(palette.tone(tone));
        }
        return toneList;
    }

    public static MONET stringToEnum(Context context, String enumString) {
        if (enumString.equals(context.getString(R.string.monet_neutral))) {
            return MONET.NEUTRAL;
        } else if (enumString.equals(context.getString(R.string.monet_monochrome))) {
            return MONET.MONOCHROME;
        } else if (enumString.equals(context.getString(R.string.monet_tonalspot))) {
            return MONET.TONALSPOT;
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
            return MONET.TONALSPOT;
        }
    }
}
