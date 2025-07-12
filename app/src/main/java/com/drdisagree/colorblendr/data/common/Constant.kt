package com.drdisagree.colorblendr.data.common

import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.google.gson.Gson

object Constant {

    // Preferences file
    private const val OWN_PACKAGE_NAME = BuildConfig.APPLICATION_ID
    const val SHARED_PREFS = "${OWN_PACKAGE_NAME}_preferences"

    // Database
    const val DATABASE_NAME = "colorblendr_database"
    const val CUSTOM_STYLE_TABLE = "custom_style_table"

    // Package names
    const val FRAMEWORK_PACKAGE = "android"
    const val SYSTEMUI_PACKAGE = "com.android.systemui"
    const val SHELL_PACKAGE = "com.android.shell"
    const val SYSTEMUI_CLOCKS = "com.android.systemui.clocks."
    const val GOOGLE_FEEDS = "com.google.android.googlequicksearchbox"
    const val GOOGLE_NEWS = "com.google.android.apps.magazines"
    const val PLAY_GAMES = "com.google.android.play.games"
    const val SETTINGS = "com.android.settings"
    const val SETTINGS_LINEAGEOS = "org.lineageos.settings"
    const val SETTINGS_SEARCH = "com.google.android.settings.intelligence"
    const val SETTINGS_SEARCH_AOSP = "com.android.settings.intelligence"
    const val LINEAGE_PARTS = "org.lineageos.lineageparts"
    const val PIXEL_LAUNCHER = "com.google.android.apps.nexuslauncher"
    const val DOLBY_ATMOS = "org.lineageos.dspvolume.xiaomi"
    const val THEME_PICKER = "com.android.wallpaper"
    const val THEME_PICKER_GOOGLE = "com.google.android.apps.wallpaper"

    // Request codes
    const val SHIZUKU_PERMISSION_REQUEST_ID = 100

    // Overlay constants
    const val FABRICATED_OVERLAY_SOURCE_PACKAGE = FRAMEWORK_PACKAGE
    const val FABRICATED_OVERLAY_NAME_SYSTEM = "${OWN_PACKAGE_NAME}_dynamic_theme"
    const val FABRICATED_OVERLAY_NAME_SYSTEMUI = "${OWN_PACKAGE_NAME}_dynamic_theme_system"
    const val FABRICATED_OVERLAY_NAME_APPS = "$OWN_PACKAGE_NAME.%s_dynamic_theme"
    const val THEME_CUSTOMIZATION_OVERLAY_PACKAGES = "theme_customization_overlay_packages"

    // General preferences
    const val FIRST_RUN = "firstRun"
    const val THEMING_ENABLED = "themingEnabled"
    const val MONET_STYLE = "customMonetStyle"
    const val CUSTOM_MONET_STYLE = "userGeneratedMonetStyle"
    const val MODE_SPECIFIC_THEMES = "modeSpecificThemes"
    const val SCREEN_OFF_UPDATE_COLORS = "screenOffUpdateColors"
    const val DARKER_LAUNCHER_ICONS = "darkerLauncherIcons"
    const val SEMI_TRANSPARENT_LAUNCHER_ICONS = "semiTransparentLauncherIcons"
    const val FORCE_PITCH_BLACK_SETTINGS = "forcePitchBlackSettings"
    const val MONET_ACCURATE_SHADES = "monetAccurateShades"
    const val MONET_PITCH_BLACK_THEME = "monetPitchBlackTheme"
    const val MONET_SEED_COLOR_ENABLED = "monetSeedColorEnabled"
    const val MONET_SEED_COLOR = "monetSeedColor"
    const val MONET_SECONDARY_COLOR = "monetSecondaryColor"
    const val MONET_TERTIARY_COLOR = "monetTertiaryColor"
    const val MANUAL_OVERRIDE_COLORS = "manualOverrideColors"
    const val MONET_LAST_UPDATED = "monetLastUpdated"
    const val MONET_STYLE_ORIGINAL_NAME = "monetStyleOriginalName"
    const val WALLPAPER_COLOR_LIST = "wallpaperColorList"
    const val FABRICATED_OVERLAY_FOR_APPS_STATE = "fabricatedOverlayForAppsState"
    const val SHOW_PER_APP_THEME_WARN = "showPerAppThemeWarn"
    const val TINT_TEXT_COLOR = "tintTextColor"
    const val SHIZUKU_THEMING_ENABLED = "shizukuThemingEnabled"
    const val APP_LIST_FILTER_METHOD = "appListFilterMethod"
    const val ADB_PAIRING_CODE = "adbPairingCode"
    const val ADB_IP = "adbIp"
    const val ADB_PAIRING_PORT = "adbPairingPort"
    const val ADB_CONNECTING_PORT = "adbConnectingPort"

    val MONET_ACCENT_SATURATION: String
        get() = if (!modeSpecificThemesEnabled()) {
            "monetAccentSaturationValue"
        } else {
            if (SystemUtil.isDarkMode) "monetAccentSaturationValue"
            else "monetAccentSaturationValueLight"
        }
    val MONET_BACKGROUND_SATURATION: String
        get() = if (!modeSpecificThemesEnabled()) {
            "monetBackgroundSaturationValue"
        } else {
            if (SystemUtil.isDarkMode) "monetBackgroundSaturationValue"
            else "monetBackgroundSaturationValueLight"
        }
    val MONET_BACKGROUND_LIGHTNESS: String
        get() = if (!modeSpecificThemesEnabled()) {
            "monetBackgroundLightnessValue"
        } else {
            if (SystemUtil.isDarkMode) "monetBackgroundLightnessValue"
            else "monetBackgroundLightnessValueLight"
        }

    @Deprecated("Use of shared preferences for saving custom styles is deprecated, use room database instead")
    const val SAVED_CUSTOM_MONET_STYLES = "savedCustomMonetStyles"

    // Service preferences
    val GSON: Gson = Gson()
    const val PREF_WORKING_METHOD = "workingMethod"

    val EXCLUDED_PREFS_FROM_BACKUP: Set<String> = HashSet(
        listOf(
            FIRST_RUN,
            PREF_WORKING_METHOD,
            MONET_LAST_UPDATED,
            THEMING_ENABLED,
            SHIZUKU_THEMING_ENABLED,
            WALLPAPER_COLOR_LIST
        )
    )

    var WORKING_METHOD: WorkMethod = WorkMethod.NULL
}