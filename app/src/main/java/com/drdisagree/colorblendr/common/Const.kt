package com.drdisagree.colorblendr.common

import android.os.Build
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.utils.RomUtil.isOneUI
import com.drdisagree.colorblendr.utils.SystemUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.atomic.AtomicInteger

object Const {
    // Preferences file
    const val SHARED_PREFS: String = BuildConfig.APPLICATION_ID + "_preferences"

    // Package names
    const val FRAMEWORK_PACKAGE: String = "android"
    const val SYSTEMUI_PACKAGE: String = "com.android.systemui"
    const val SHELL_PACKAGE: String = "com.android.shell"
    const val SYSTEMUI_CLOCKS: String = "com.android.systemui.clocks."
    const val GOOGLE_FEEDS: String = "com.google.android.googlequicksearchbox"
    const val GOOGLE_NEWS: String = "com.google.android.apps.magazines"
    const val PLAY_GAMES: String = "com.google.android.play.games"
    const val SETTINGS: String = "com.android.settings"
    const val SETTINGS_SEARCH: String = "com.google.android.settings.intelligence"
    const val PIXEL_LAUNCHER: String = "com.google.android.apps.nexuslauncher"

    // General preferences
    const val FIRST_RUN: String = "firstRun"
    const val THEMING_ENABLED: String = "themingEnabled"
    const val MONET_STYLE: String = "customMonetStyle"
    const val MODE_SPECIFIC_THEMES: String = "modeSpecificThemes"
    const val SCREEN_OFF_UPDATE_COLORS: String = "screenOffUpdateColors"
    const val DARKER_LAUNCHER_ICONS: String = "darkerLauncherIcons"
    const val SEMI_TRANSPARENT_LAUNCHER_ICONS: String = "semiTransparentLauncherIcons"
    const val FORCE_PITCH_BLACK_SETTINGS: String = "forcePitchBlackSettings"
    private val modeSpecificThemes: Boolean
        get() = RPrefs.getBoolean(MODE_SPECIFIC_THEMES, false)
    val MONET_ACCENT_SATURATION: String
        get() = if (!modeSpecificThemes) {
            "monetAccentSaturationValue"
        } else {
            if (SystemUtil.isDarkMode) "monetAccentSaturationValue" else "monetAccentSaturationValueLight"
        }
    val MONET_BACKGROUND_SATURATION: String
        get() = if (!modeSpecificThemes) {
            "monetBackgroundSaturationValue"
        } else {
            if (SystemUtil.isDarkMode) "monetBackgroundSaturationValue" else "monetBackgroundSaturationValueLight"
        }
    val MONET_BACKGROUND_LIGHTNESS: String
        get() = if (!modeSpecificThemes) {
            "monetBackgroundLightnessValue"
        } else {
            if (SystemUtil.isDarkMode) "monetBackgroundLightnessValue" else "monetBackgroundLightnessValueLight"
        }
    const val MONET_ACCURATE_SHADES: String = "monetAccurateShades"
    const val MONET_PITCH_BLACK_THEME: String = "monetPitchBlackTheme"
    const val MONET_SEED_COLOR_ENABLED: String = "monetSeedColorEnabled"
    const val MONET_SEED_COLOR: String = "monetSeedColor"
    const val MONET_SECONDARY_COLOR: String = "monetSecondaryColor"
    const val MONET_TERTIARY_COLOR: String = "monetTertiaryColor"
    const val MANUAL_OVERRIDE_COLORS: String = "manualOverrideColors"
    const val MONET_LAST_UPDATED: String = "monetLastUpdated"
    const val MONET_STYLE_ORIGINAL_NAME: String = "monetStyleOriginalName"
    const val FABRICATED_OVERLAY_SOURCE_PACKAGE: String = FRAMEWORK_PACKAGE
    const val FABRICATED_OVERLAY_NAME_SYSTEM: String = "${BuildConfig.APPLICATION_ID}_dynamic_theme"
    const val FABRICATED_OVERLAY_NAME_SYSTEMUI: String =
        "${BuildConfig.APPLICATION_ID}_dynamic_theme_system"
    const val FABRICATED_OVERLAY_NAME_APPS: String =
        "${BuildConfig.APPLICATION_ID}.%s_dynamic_theme"
    const val WALLPAPER_COLOR_LIST: String = "wallpaperColorList"
    private const val FABRICATED_OVERLAY_FOR_APPS_STATE: String = "fabricatedOverlayForAppsState"
    const val SHOW_PER_APP_THEME_WARN: String = "showPerAppThemeWarn"
    const val TINT_TEXT_COLOR: String = "tintTextColor"
    const val SHIZUKU_PERMISSION_REQUEST_ID: Int = 100
    const val SHIZUKU_THEMING_ENABLED: String = "shizukuThemingEnabled"
    const val APP_LIST_FILTER_METHOD: String = "appListFilterMethod"
    val screenOrientation: AtomicInteger = AtomicInteger(-1)

    // AOSP key
    const val THEME_CUSTOMIZATION_OVERLAY_PACKAGES: String = "theme_customization_overlay_packages"

    // Samsung key
    const val WALLPAPER_THEME_STATE: String = "wallpapertheme_state"
    const val WALLPAPER_THEME_COLOR_IS_GRAY: String = "wallpapertheme_color_isgray"
    const val LOCK_ADAPTIVE_COLOR: String = "lock_adaptive_color"
    const val WALLPAPER_THEME_COLORS: String = "wallpapertheme_color"
    const val WALLPAPER_THEME_COLORS_FOR_GOOGLE: String = "wallpapertheme_color_for_g"
    const val COLOR_THEME_APP_ICON: String = "colortheme_app_icon"

    // Service preferences
    val GSON: Gson = Gson()
    private const val PREF_WORKING_METHOD: String = "workingMethod"

    var EXCLUDED_PREFS_FROM_BACKUP: Set<String> = HashSet(
        listOf(
            FIRST_RUN,
            PREF_WORKING_METHOD,
            MONET_LAST_UPDATED,
            THEMING_ENABLED,
            SHIZUKU_THEMING_ENABLED,
            WALLPAPER_COLOR_LIST
        )
    )

    fun saveSelectedFabricatedApps(selectedApps: HashMap<String, Boolean>) {
        RPrefs.putString(FABRICATED_OVERLAY_FOR_APPS_STATE, GSON.toJson(selectedApps))
    }

    val selectedFabricatedApps: HashMap<String, Boolean>
        get() {
            val selectedApps = RPrefs.getString(
                FABRICATED_OVERLAY_FOR_APPS_STATE,
                null
            )
            if (selectedApps.isNullOrEmpty()) {
                return HashMap()
            }

            return GSON.fromJson(
                selectedApps,
                object :
                    TypeToken<HashMap<String?, Boolean?>?>() {
                }.type
            )
        }

    var WORKING_METHOD: WorkMethod = WorkMethod.NULL

    val workingMethod: WorkMethod
        get() = WorkMethod.fromString(
            RPrefs.getString(
                PREF_WORKING_METHOD,
                WorkMethod.NULL.toString()
            )
        )

    fun saveWorkingMethod(workMethod: WorkMethod) {
        RPrefs.putString(PREF_WORKING_METHOD, workMethod.toString())
    }

    // Working method of app
    enum class WorkMethod {
        NULL,
        ROOT,
        SHIZUKU;

        companion object {
            fun fromString(str: String?): WorkMethod {
                return try {
                    valueOf(str!!)
                } catch (e: Exception) {
                    NULL
                }
            }
        }
    }

    enum class AppType {
        SYSTEM,
        USER,
        LAUNCHABLE,
        ALL
    }

    val isRootMode: Boolean = workingMethod == Const.WorkMethod.ROOT
    val isShizukuMode: Boolean = workingMethod == Const.WorkMethod.SHIZUKU
    val isOneUIShizukuMode: Boolean = isOneUI && workingMethod == Const.WorkMethod.SHIZUKU

    val hasPixelLauncher: Boolean = SystemUtil.isAppInstalled(PIXEL_LAUNCHER)

    val isAtleastA13 = !isShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isAtleastA14 = !isShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
