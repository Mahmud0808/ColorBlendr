package com.drdisagree.colorblendr.ui.repositories;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.content.Context;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;

public class ThemeRepository {

    private final Context context;

    public ThemeRepository(Context context) {
        this.context = context;
    }

    public int getAccentSaturation() {
        return RPrefs.getInt(MONET_ACCENT_SATURATION, 100);
    }

    public void setAccentSaturation(int value) {
        RPrefs.putInt(MONET_ACCENT_SATURATION, value);
    }

    public void resetAccentSaturation() {
        RPrefs.clearPref(MONET_ACCENT_SATURATION);
    }

    public int getBackgroundSaturation() {
        return RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100);
    }

    public void setBackgroundSaturation(int value) {
        RPrefs.putInt(MONET_BACKGROUND_SATURATION, value);
    }

    public void resetBackgroundSaturation() {
        RPrefs.clearPref(MONET_BACKGROUND_SATURATION);
    }

    public int getBackgroundLightness() {
        return RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100);
    }

    public void setBackgroundLightness(int value) {
        RPrefs.putInt(MONET_BACKGROUND_LIGHTNESS, value);
    }

    public void resetBackgroundLightness() {
        RPrefs.clearPref(MONET_BACKGROUND_LIGHTNESS);
    }

    public boolean getPitchBlackTheme() {
        return RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false);
    }

    public boolean getAccurateShades() {
        return RPrefs.getBoolean(MONET_ACCURATE_SHADES, true);
    }

    public ColorSchemeUtil.MONET getMonetStyle() {
        return ColorSchemeUtil.stringToEnumMonetStyle(
                context,
                RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
        );
    }

    public void setMonetLastUpdated() {
        RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
    }

    public boolean isNotShizukuMode() {
        return Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;
    }
}
