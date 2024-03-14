package com.drdisagree.colorblendr.extension;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE_ORIGINAL_NAME;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorUtil;

import org.json.JSONObject;

public class ThemeOverlayPackage {

    private static final String TAG = ThemeOverlayPackage.class.getSimpleName();
    public static final String THEME_STYLE = "android.theme.customization.theme_style";
    public static final String COLOR_SOURCE = "android.theme.customization.color_source";
    public static final String SYSTEM_PALETTE = "android.theme.customization.system_palette";
    public static final String ACCENT_COLOR = "android.theme.customization.accent_color";
    public static final String COLOR_BOTH = "android.theme.customization.color_both";
    public static final String APPLIED_TIMESTAMP = "_applied_timestamp";

    public static JSONObject getThemeCustomizationOverlayPackages() {
        JSONObject object = new JSONObject();

        try {
            object.putOpt(
                    THEME_STYLE,
                    RPrefs.getString(MONET_STYLE_ORIGINAL_NAME, "TONAL_SPOT")
            );
            object.putOpt(COLOR_SOURCE, "preset");
            object.putOpt(
                    SYSTEM_PALETTE,
                    ColorUtil.intToHexColorNoHash(RPrefs.getInt(MONET_SEED_COLOR, Color.BLUE))
            );
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                object.putOpt(
                        ACCENT_COLOR,
                        ColorUtil.intToHexColorNoHash(RPrefs.getInt(MONET_SEED_COLOR, Color.BLUE))
                );
            }
            object.putOpt(APPLIED_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        } catch (Exception e) {
            Log.e(TAG, "getThemeCustomizationOverlayPackages:", e);
        }

        return object;
    }
}
