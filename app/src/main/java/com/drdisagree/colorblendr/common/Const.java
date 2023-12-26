package com.drdisagree.colorblendr.common;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.xposed.utils.BootLoopProtector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Const {

    public static final String SharedPrefs = BuildConfig.APPLICATION_ID + "_preferences";
    public static final List<String> PREF_UPDATE_EXCLUSIONS = Arrays.asList(BootLoopProtector.LOAD_TIME_KEY, BootLoopProtector.PACKAGE_STRIKE_KEY);
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final String TAB_SELECTED_INDEX = "tabSelectedIndex";
    public static final String MONET_STYLE = "customMonetStyle";
    public static final String MONET_ACCENT_SATURATION = "monetAccentSaturationValue";
    public static final String MONET_BACKGROUND_SATURATION = "monetBackgroundSaturationValue";
    public static final String MONET_BACKGROUND_LIGHTNESS = "monetBackgroundLightnessValue";
    public static final String MONET_ACCURATE_SHADES = "monetAccurateShades";
    public static final String MONET_PITCH_BLACK_THEME = "monetPitchBlackTheme";
    public static final String MONET_SEED_COLOR = "monetSeedColor";
    public static final String MONET_SEED_COLOR_ENABLED = "monetSeedColorEnabled";
    public static final String MANUAL_OVERRIDE_COLORS = "manualOverrideColors";
    public static boolean isBackgroundServiceRunning = false;
    public static final String MONET_LAST_UPDATED = "monetLastUpdated";
    public static final String FABRICATED_OVERLAY_SOURCE_PACKAGE = FRAMEWORK_PACKAGE;
    public static final String FABRICATED_OVERLAY_NAME = BuildConfig.APPLICATION_ID + "_dynamic_theme";
    public static final String PREF_WORKING_METHOD = "workingMethod";
    public static WORK_METHOD WORKING_METHOD = WORK_METHOD.fromString(RPrefs.getString(PREF_WORKING_METHOD, WORK_METHOD.NULL.toString()));

    public static void saveWorkingMethod() {
        RPrefs.putString(PREF_WORKING_METHOD, WORKING_METHOD.toString());
    }

    public enum WORK_METHOD {
        NULL,
        ROOT,
        SHIZUKU,
        XPOSED;

        public static WORK_METHOD fromString(String str) {
            try {
                return valueOf(str);
            } catch (Exception e) {
                return NULL;
            }
        }
    }

    public static Set<String> EXCLUDED_PREFS_FROM_BACKUP = new HashSet<>(
            Arrays.asList(
                    PREF_WORKING_METHOD,
                    MONET_LAST_UPDATED,
                    TAB_SELECTED_INDEX
            )
    );
}
