package com.drdisagree.colorblendr.common

import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.config.RPrefs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.atomic.AtomicInteger

object Const {
    // Preferences file
    const val SHARED_PREFS: String = BuildConfig.APPLICATION_ID + "_preferences"

    // System packages
    const val FRAMEWORK_PACKAGE: String = "android"
    const val SYSTEMUI_PACKAGE: String = "com.android.systemui"
    const val SHELL_PACKAGE: String = "com.android.shell"

    // General preferences
    const val FIRST_RUN: String = "firstRun"
    const val THEMING_ENABLED: String = "themingEnabled"
    const val MONET_STYLE: String = "customMonetStyle"
    const val MONET_ACCENT_SATURATION: String = "monetAccentSaturationValue"
    const val MONET_BACKGROUND_SATURATION: String = "monetBackgroundSaturationValue"
    const val MONET_BACKGROUND_LIGHTNESS: String = "monetBackgroundLightnessValue"
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
    const val THEME_CUSTOMIZATION_OVERLAY_PACKAGES: String = "theme_customization_overlay_packages"
    const val SHIZUKU_THEMING_ENABLED: String = "shizukuThemingEnabled"
    const val APP_LIST_FILTER_METHOD: String = "appListFilterMethod"
    val screenOrientation: AtomicInteger = AtomicInteger(-1)

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
}
