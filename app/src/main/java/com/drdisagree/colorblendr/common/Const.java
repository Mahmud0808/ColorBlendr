package com.drdisagree.colorblendr.common;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.xposed.utils.BootLoopProtector;

import java.util.Arrays;
import java.util.List;

public class Const {

    public static final String SharedPrefs = BuildConfig.APPLICATION_ID + "_preferences";
    public static final List<String> PREF_UPDATE_EXCLUSIONS = Arrays.asList(BootLoopProtector.LOAD_TIME_KEY, BootLoopProtector.PACKAGE_STRIKE_KEY);
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final String MONET_STYLE = "customMonetStyle";
    public static final String MONET_ACCENT_SATURATION = "monetAccentSaturationValue";
    public static final String MONET_BACKGROUND_SATURATION = "monetBackgroundSaturationValue";
    public static final String MONET_BACKGROUND_LIGHTNESS = "monetBackgroundLightnessValue";
    public static final String MONET_ACCURATE_SHADES = "monetAccurateShades";
    public static final String MONET_PITCH_BLACK_THEME = "monetPitchBlackTheme";
}
