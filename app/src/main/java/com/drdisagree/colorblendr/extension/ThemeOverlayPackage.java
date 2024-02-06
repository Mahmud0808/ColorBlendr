package com.drdisagree.colorblendr.extension;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE_ORIGINAL_NAME;

import android.graphics.Color;
import android.util.Log;

import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorUtil;

import org.json.JSONObject;

public class ThemeOverlayPackage {

    private static final String TAG = ThemeOverlayPackage.class.getSimpleName();
    private static final String THEME_STYLE = "android.theme.customization.theme_style";
    private static final String COLOR_SOURCE = "android.theme.customization.color_source";
    private static final String SYSTEM_PALETTE = "android.theme.customization.system_palette";

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
        } catch (Exception e) {
            Log.e(TAG, "getThemeCustomizationOverlayPackages:", e);
        }

        return object;
    }
}
