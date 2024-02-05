package com.drdisagree.colorblendr.extension;

import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.content.Context;
import android.graphics.Color;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ThemeOverlayPackage {

    private static final String THEME_STYLE = "android.theme.customization.theme_style";
    private static final String COLOR_SOURCE = "android.theme.customization.color_source";
    private static final String SYSTEM_PALETTE = "android.theme.customization.system_palette";

    public static JSONObject getThemeCustomizationOverlayPackages() {
        Context mContext = ColorBlendr.getAppContext();
        JSONObject object = new JSONObject();

        try {
            object.putOpt(THEME_STYLE, getOriginalName(
                    RPrefs.getString(MONET_STYLE, mContext.getString(R.string.monet_tonalspot))
            ));
            object.putOpt(COLOR_SOURCE, "preset");
            object.putOpt(SYSTEM_PALETTE, ColorUtil.intToHexColorNoHash(RPrefs.getInt(MONET_SEED_COLOR, Color.BLUE)));
        } catch (JSONException ignored) {
        }

        return object;
    }

    private static String getOriginalName(String name) {
        Context mContext = ColorBlendr.getAppContext();

        if (name.equals(mContext.getString(R.string.monet_neutral))) {
            return "SPRITZ";
        } else if (name.equals(mContext.getString(R.string.monet_vibrant))) {
            return "VIBRANT";
        } else if (name.equals(mContext.getString(R.string.monet_expressive))) {
            return "EXPRESSIVE";
        } else if (name.equals(mContext.getString(R.string.monet_rainbow))) {
            return "RAINBOW";
        } else if (name.equals(mContext.getString(R.string.monet_fruitsalad))) {
            return "FRUIT_SALAD";
        } else if (name.equals(mContext.getString(R.string.monet_content))) {
            return "CONTENT";
        } else if (name.equals(mContext.getString(R.string.monet_monochrome))) {
            return "MONOCHROMATIC";
        } else if (name.equals(mContext.getString(R.string.monet_fidelity))) {
            return "FIDELITY";
        } else {
            return "TONAL_SPOT";
        }
    }
}
