package com.drdisagree.colorblendr.common

import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.utils.SystemUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Const {
    // Preferences file
    private const val OWN_PACKAGE_NAME = BuildConfig.APPLICATION_ID
    const val SHARED_PREFS = "${OWN_PACKAGE_NAME}_preferences"

    // Package names
    const val FRAMEWORK_PACKAGE = "android"
    const val SYSTEMUI_PACKAGE = "com.android.systemui"
    const val SHELL_PACKAGE = "com.android.shell"
    const val SYSTEMUI_CLOCKS = "com.android.systemui.clocks."
    const val GOOGLE_FEEDS = "com.google.android.googlequicksearchbox"
    const val GOOGLE_NEWS = "com.google.android.apps.magazines"
    const val PLAY_GAMES = "com.google.android.play.games"
    const val SETTINGS = "com.android.settings"
    const val SETTINGS_SEARCH = "com.google.android.settings.intelligence"
    const val PIXEL_LAUNCHER = "com.google.android.apps.nexuslauncher"

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
    const val MONET_ACCURATE_SHADES = "monetAccurateShades"
    const val MONET_PITCH_BLACK_THEME = "monetPitchBlackTheme"
    const val MONET_SEED_COLOR_ENABLED = "monetSeedColorEnabled"
    const val MONET_SEED_COLOR = "monetSeedColor"
    const val MONET_SECONDARY_COLOR = "monetSecondaryColor"
    const val MONET_TERTIARY_COLOR = "monetTertiaryColor"
    const val MANUAL_OVERRIDE_COLORS = "manualOverrideColors"
    const val MONET_LAST_UPDATED = "monetLastUpdated"
    const val MONET_STYLE_ORIGINAL_NAME = "monetStyleOriginalName"
    const val FABRICATED_OVERLAY_SOURCE_PACKAGE = FRAMEWORK_PACKAGE
    const val FABRICATED_OVERLAY_NAME_SYSTEM = "${OWN_PACKAGE_NAME}_dynamic_theme"
    const val FABRICATED_OVERLAY_NAME_SYSTEMUI = "${OWN_PACKAGE_NAME}_dynamic_theme_system"
    const val FABRICATED_OVERLAY_NAME_APPS = "$OWN_PACKAGE_NAME.%s_dynamic_theme"
    const val WALLPAPER_COLOR_LIST = "wallpaperColorList"
    private const val FABRICATED_OVERLAY_FOR_APPS_STATE = "fabricatedOverlayForAppsState"
    const val SHOW_PER_APP_THEME_WARN = "showPerAppThemeWarn"
    const val TINT_TEXT_COLOR = "tintTextColor"
    const val SHIZUKU_PERMISSION_REQUEST_ID = 100
    const val THEME_CUSTOMIZATION_OVERLAY_PACKAGES = "theme_customization_overlay_packages"
    const val SHIZUKU_THEMING_ENABLED = "shizukuThemingEnabled"
    const val APP_LIST_FILTER_METHOD = "appListFilterMethod"
    const val SAVED_CUSTOM_MONET_STYLES = "savedCustomMonetStyles"

    // Service preferences
    val GSON: Gson = Gson()
    private const val PREF_WORKING_METHOD = "workingMethod"

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
}
