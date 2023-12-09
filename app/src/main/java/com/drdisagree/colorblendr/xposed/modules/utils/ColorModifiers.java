package com.drdisagree.colorblendr.xposed.modules.utils;

import android.graphics.Color;

import com.drdisagree.colorblendr.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ColorModifiers {

    public static ArrayList<Integer> generateShades(float hue, float chroma) {
        ArrayList<Integer> shadeList = new ArrayList<>(Arrays.asList(new Integer[12]));

        shadeList.set(0, ColorUtil.CAMToColor(hue, Math.min(40.0f, chroma), 99.0f));
        shadeList.set(1, ColorUtil.CAMToColor(hue, Math.min(40.0f, chroma), 95.0f));

        for (int i = 2; i < 12; i++) {
            float lstar;
            if (i == 6) {
                lstar = 49.6f;
            } else {
                lstar = 100 - ((i - 1) * 10);
            }
            shadeList.set(i, ColorUtil.CAMToColor(hue, chroma, lstar));
        }

        return shadeList;
    }

    public static ArrayList<Integer> modifyColors(
            ArrayList<Integer> palette,
            AtomicInteger counter,
            int monetAccentSaturation,
            int monetBackgroundSaturation,
            int monetBackgroundLightness,
            boolean pitchBlackTheme
    ) {
        counter.getAndIncrement();

        boolean accentPalette = counter.get() <= 3;

        boolean accentSaturation = monetAccentSaturation != 100;
        boolean backgroundSaturation = monetBackgroundSaturation != 100;
        boolean backgroundLightness = monetBackgroundLightness != 100;

        if (accentPalette) {
            if (accentSaturation) {
                // Set accent saturation
                palette.replaceAll(o -> ColorUtil.modifySaturation(o, monetAccentSaturation));
            }
        } else {
            if (backgroundSaturation) {
                // Set background saturation
                palette.replaceAll(o -> ColorUtil.modifySaturation(o, monetBackgroundSaturation));
            }

            if (backgroundLightness) {
                // Set background lightness
                for (int j = 0; j < palette.size(); j++) {
                    palette.set(j, ColorUtil.modifyLightness(palette.get(j), monetBackgroundLightness, j + 1));
                }
            }

            if (pitchBlackTheme) {
                // Set pitch black theme
                palette.set(10, Color.BLACK);
            }
        }

        if (counter.get() >= 5) {
            counter.set(0);
        }

        return palette;
    }

    public static <K, V> Map<K, V> zipToMap(List<K> keys, List<V> values) {
        Map<K, V> result = new HashMap<>();

        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Lists must have the same size. Provided keys size: " + keys.size() + " Provided values size: " + values.size() + ".");
        }

        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }

        return result;
    }
}
