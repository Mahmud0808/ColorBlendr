package me.jfenn.colorpickerdialog.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

public class ColorUtils {

    public static boolean isColorDark(@ColorInt int color) {
        return getColorDarkness(color) > 0.4;
    }

    private static double getColorDarkness(@ColorInt int color) {
        if (color == Color.BLACK) return 1.0;
        else if (color == Color.WHITE || color == Color.TRANSPARENT) return 0.0;
        return (1 - (0.259 * Color.red(color) + 0.667 * Color.green(color) + 0.074 * Color.blue(color)) / 255);
    }

    @ColorInt
    public static int withBackground(@ColorInt int color, @ColorInt int background) {
        float alpha = Color.alpha(color) / 255f;
        return Color.rgb(
                (int) ((Color.red(color) * alpha) + (Color.red(background) * (1 - alpha))),
                (int) ((Color.green(color) * alpha) + (Color.green(background) * (1 - alpha))),
                (int) ((Color.blue(color) * alpha) + (Color.blue(background) * (1 - alpha)))
        );
    }

    @ColorInt
    public static int fromAttr(Context context, @AttrRes int attr, @ColorInt int defaultColor) {
        TypedValue out = new TypedValue();
        try {
            context.getTheme().resolveAttribute(attr, out, true);
            if (out.resourceId == 0)
                return out.data == 0 ? defaultColor : out.data;
            else return ContextCompat.getColor(context, out.resourceId);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    @ColorInt
    public static int fromAttrRes(Context context, @AttrRes int attr, @ColorRes int defaultColorRes) {
        TypedValue out = new TypedValue();
        try {
            context.getTheme().resolveAttribute(attr, out, true);
            if (out.resourceId == 0)
                return out.data == 0 ? ContextCompat.getColor(context, defaultColorRes) : out.data;
            else return ContextCompat.getColor(context, out.resourceId);
        } catch (Exception e) {
            return ContextCompat.getColor(context, defaultColorRes);
        }
    }

    public static int[] getColorWheelArr(float saturation, float brightness) {
        int[] arr = new int[13];
        for (int i = 0; i <= 12; i++)
            arr[i] = Color.HSVToColor(new float[]{i * 30, saturation, brightness});

        return arr;
    }

}