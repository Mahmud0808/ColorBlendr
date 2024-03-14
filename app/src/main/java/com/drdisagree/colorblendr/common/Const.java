package com.drdisagree.colorblendr.common;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.config.RPrefs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Const {

    // Preferences file
    public static final String SharedPrefs = BuildConfig.APPLICATION_ID + "_preferences";

    // System packages
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final String SHELL_PACKAGE = "com.android.shell";

    // General preferences
    public static final String FIRST_RUN = "firstRun";
    public static final String THEMING_ENABLED = "themingEnabled";
    public static final String MONET_STYLE = "customMonetStyle";
    public static final String MONET_ACCENT_SATURATION = "monetAccentSaturationValue";
    public static final String MONET_BACKGROUND_SATURATION = "monetBackgroundSaturationValue";
    public static final String MONET_BACKGROUND_LIGHTNESS = "monetBackgroundLightnessValue";
    public static final String MONET_ACCURATE_SHADES = "monetAccurateShades";
    public static final String MONET_PITCH_BLACK_THEME = "monetPitchBlackTheme";
    public static final String MONET_SEED_COLOR = "monetSeedColor";
    public static final String MONET_SEED_COLOR_ENABLED = "monetSeedColorEnabled";
    public static final String MANUAL_OVERRIDE_COLORS = "manualOverrideColors";
    public static final String MONET_LAST_UPDATED = "monetLastUpdated";
    public static final String MONET_STYLE_ORIGINAL_NAME = "monetStyleOriginalName";
    public static final String FABRICATED_OVERLAY_SOURCE_PACKAGE = FRAMEWORK_PACKAGE;
    public static final String FABRICATED_OVERLAY_NAME_SYSTEM = BuildConfig.APPLICATION_ID + "_dynamic_theme";
    public static final String FABRICATED_OVERLAY_NAME_SYSTEMUI = BuildConfig.APPLICATION_ID + "_dynamic_theme_system";
    public static final String FABRICATED_OVERLAY_NAME_APPS = BuildConfig.APPLICATION_ID + ".%s_dynamic_theme";
    public static final String WALLPAPER_COLOR_LIST = "wallpaperColorList";
    public static final String FABRICATED_OVERLAY_FOR_APPS_STATE = "fabricatedOverlayForAppsState";
    public static final String SHOW_PER_APP_THEME_WARN = "showPerAppThemeWarn";
    public static final String TINT_TEXT_COLOR = "tintTextColor";
    public static final int SHIZUKU_PERMISSION_REQUEST_ID = 100;
    public static final String THEME_CUSTOMIZATION_OVERLAY_PACKAGES = "theme_customization_overlay_packages";
    public static final String SHIZUKU_THEMING_ENABLED = "shizukuThemingEnabled";

    // Service preferences
    public static final Gson GSON = new Gson();
    public static final String PREF_WORKING_METHOD = "workingMethod";

    public static Set<String> EXCLUDED_PREFS_FROM_BACKUP = new HashSet<>(
            Arrays.asList(
                    PREF_WORKING_METHOD,
                    MONET_LAST_UPDATED
            )
    );

    public static void saveSelectedFabricatedApps(HashMap<String, Boolean> selectedApps) {
        RPrefs.putString(FABRICATED_OVERLAY_FOR_APPS_STATE, Const.GSON.toJson(selectedApps));
    }

    public static HashMap<String, Boolean> getSelectedFabricatedApps() {
        String selectedApps = RPrefs.getString(FABRICATED_OVERLAY_FOR_APPS_STATE, null);
        if (selectedApps == null || selectedApps.isEmpty()) {
            return new HashMap<>();
        }

        return Const.GSON.fromJson(selectedApps, new TypeToken<HashMap<String, Boolean>>() {
        }.getType());
    }

    // Working method of app
    public enum WORK_METHOD {
        NULL,
        ROOT,
        SHIZUKU;

        public static WORK_METHOD fromString(String str) {
            try {
                return valueOf(str);
            } catch (Exception e) {
                return NULL;
            }
        }
    }

    public static WORK_METHOD WORKING_METHOD = WORK_METHOD.NULL;

    public static WORK_METHOD getWorkingMethod() {
        return WORK_METHOD.fromString(RPrefs.getString(PREF_WORKING_METHOD, WORK_METHOD.NULL.toString()));
    }

    public static void saveWorkingMethod(WORK_METHOD workMethod) {
        RPrefs.putString(PREF_WORKING_METHOD, workMethod.toString());
    }
}
