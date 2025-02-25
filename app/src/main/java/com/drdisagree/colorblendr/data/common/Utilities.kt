package com.drdisagree.colorblendr.data.common

import android.graphics.Color
import com.drdisagree.colorblendr.data.common.Constant.APP_LIST_FILTER_METHOD
import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.DARKER_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_FOR_APPS_STATE
import com.drdisagree.colorblendr.data.common.Constant.FIRST_RUN
import com.drdisagree.colorblendr.data.common.Constant.FORCE_PITCH_BLACK_SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.GSON
import com.drdisagree.colorblendr.data.common.Constant.MANUAL_OVERRIDE_COLORS
import com.drdisagree.colorblendr.data.common.Constant.MODE_SPECIFIC_THEMES
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.data.common.Constant.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.data.common.Constant.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.data.common.Constant.MONET_SECONDARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.MONET_STYLE_ORIGINAL_NAME
import com.drdisagree.colorblendr.data.common.Constant.MONET_TERTIARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.PREF_WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Constant.SCREEN_OFF_UPDATE_COLORS
import com.drdisagree.colorblendr.data.common.Constant.SEMI_TRANSPARENT_LAUNCHER_ICONS
import com.drdisagree.colorblendr.data.common.Constant.SHIZUKU_THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.SHOW_PER_APP_THEME_WARN
import com.drdisagree.colorblendr.data.common.Constant.THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.TINT_TEXT_COLOR
import com.drdisagree.colorblendr.data.common.Constant.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.config.Prefs.clearPref
import com.drdisagree.colorblendr.data.config.Prefs.getBoolean
import com.drdisagree.colorblendr.data.config.Prefs.getInt
import com.drdisagree.colorblendr.data.config.Prefs.getLong
import com.drdisagree.colorblendr.data.config.Prefs.getString
import com.drdisagree.colorblendr.data.config.Prefs.putBoolean
import com.drdisagree.colorblendr.data.config.Prefs.putInt
import com.drdisagree.colorblendr.data.config.Prefs.putLong
import com.drdisagree.colorblendr.data.config.Prefs.putString
import com.drdisagree.colorblendr.data.database.AppDatabase
import com.drdisagree.colorblendr.data.enums.AppType
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.enums.MONET.Companion.toEnumMonet
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.data.repository.CustomStyleRepository
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import com.google.gson.reflect.TypeToken

object Utilities {

    fun isFirstRun(): Boolean {
        return getBoolean(FIRST_RUN, true)
    }

    fun setFirstRunCompleted() {
        putBoolean(FIRST_RUN, false)
    }

    fun isThemingEnabled(defaultValue: Boolean = true): Boolean {
        return getBoolean(THEMING_ENABLED, defaultValue)
    }

    fun setThemingEnabled(enabled: Boolean) {
        putBoolean(THEMING_ENABLED, enabled)
    }

    fun isShizukuThemingEnabled(defaultValue: Boolean = true): Boolean {
        return getBoolean(SHIZUKU_THEMING_ENABLED, defaultValue)
    }

    fun setShizukuThemingEnabled(enabled: Boolean) {
        putBoolean(SHIZUKU_THEMING_ENABLED, enabled)
    }

    private fun getWorkingMethod(): WorkMethod {
        return WorkMethod.fromString(getString(PREF_WORKING_METHOD, WorkMethod.NULL.toString()))
    }

    fun setWorkingMethod(workMethod: WorkMethod) {
        putString(PREF_WORKING_METHOD, workMethod.toString())
    }

    fun rootModeSelected(): Boolean {
        return WORKING_METHOD == WorkMethod.ROOT
    }

    fun isRootMode(): Boolean {
        return getWorkingMethod() == WorkMethod.ROOT
    }

    fun isShizukuMode(): Boolean {
        return getWorkingMethod() == WorkMethod.SHIZUKU
    }

    fun isRootOrShizukuUnknown(): Boolean {
        return getWorkingMethod() == WorkMethod.NULL
    }

    fun getCurrentMonetStyle(): MONET {
        return getString(MONET_STYLE, null).toEnumMonet()
    }

    fun setCurrentMonetStyle(monet: MONET) {
        putString(MONET_STYLE, monet.toString())
    }

    fun getCurrentCustomStyle(): String? {
        return getString(CUSTOM_MONET_STYLE, null)
    }

    fun setCurrentCustomStyle(styleId: String) {
        putString(CUSTOM_MONET_STYLE, styleId)
    }

    fun resetCustomStyle() {
        clearPref(CUSTOM_MONET_STYLE)
    }

    fun resetCustomStyleIfNotNull() {
        if (getCurrentCustomStyle() != null) {
            resetCustomStyle()
        }
    }

    fun modeSpecificThemesEnabled(): Boolean {
        return getBoolean(MODE_SPECIFIC_THEMES, false)
    }

    fun setModeSpecificThemesEnabled(enabled: Boolean) {
        putBoolean(MODE_SPECIFIC_THEMES, enabled)
    }

    fun screenOffColorUpdateEnabled(): Boolean {
        return getBoolean(SCREEN_OFF_UPDATE_COLORS, false)
    }

    fun setScreenOffColorUpdateEnabled(enabled: Boolean) {
        putBoolean(SCREEN_OFF_UPDATE_COLORS, enabled)
    }

    fun darkerLauncherIconsEnabled(): Boolean {
        return getBoolean(DARKER_LAUNCHER_ICONS, false)
    }

    fun setDarkerLauncherIconsEnabled(enabled: Boolean) {
        putBoolean(DARKER_LAUNCHER_ICONS, enabled)
    }

    fun semiTransparentLauncherIconsEnabled(): Boolean {
        return getBoolean(SEMI_TRANSPARENT_LAUNCHER_ICONS, false)
    }

    fun setSemiTransparentLauncherIconsEnabled(enabled: Boolean) {
        putBoolean(SEMI_TRANSPARENT_LAUNCHER_ICONS, enabled)
    }

    fun forcePitchBlackSettingsEnabled(): Boolean {
        return getBoolean(FORCE_PITCH_BLACK_SETTINGS, false)
    }

    fun setForcePitchBlackSettingsEnabled(enabled: Boolean) {
        putBoolean(FORCE_PITCH_BLACK_SETTINGS, enabled)
    }

    fun getAccentSaturation(): Int {
        return getInt(MONET_ACCENT_SATURATION, 100)
    }

    fun setAccentSaturation(value: Int) {
        putInt(MONET_ACCENT_SATURATION, value)
    }

    fun resetAccentSaturation() {
        clearPref(MONET_ACCENT_SATURATION)
    }

    fun getBackgroundSaturation(): Int {
        return getInt(MONET_BACKGROUND_SATURATION, 100)
    }

    fun setBackgroundSaturation(value: Int) {
        putInt(MONET_BACKGROUND_SATURATION, value)
    }

    fun resetBackgroundSaturation() {
        clearPref(MONET_BACKGROUND_SATURATION)
    }

    fun getBackgroundLightness(): Int {
        return getInt(MONET_BACKGROUND_LIGHTNESS, 100)
    }

    fun setBackgroundLightness(value: Int) {
        putInt(MONET_BACKGROUND_LIGHTNESS, value)
    }

    fun resetBackgroundLightness() {
        clearPref(MONET_BACKGROUND_LIGHTNESS)
    }

    fun accurateShadesEnabled(): Boolean {
        return getBoolean(MONET_ACCURATE_SHADES, true)
    }

    fun setAccurateShadesEnabled(enabled: Boolean) {
        putBoolean(MONET_ACCURATE_SHADES, enabled)
    }

    fun pitchBlackThemeEnabled(): Boolean {
        return getBoolean(MONET_PITCH_BLACK_THEME, false)
    }

    fun setPitchBlackThemeEnabled(enabled: Boolean) {
        putBoolean(MONET_PITCH_BLACK_THEME, enabled)
    }

    fun customColorEnabled(): Boolean {
        return getBoolean(MONET_SEED_COLOR_ENABLED, false)
    }

    fun setCustomColorEnabled(enabled: Boolean) {
        putBoolean(MONET_SEED_COLOR_ENABLED, enabled)
    }

    fun getSeedColorValue(defaultValue: Int = Int.MIN_VALUE): Int {
        return getInt(MONET_SEED_COLOR, defaultValue)
    }

    fun setSeedColorValue(color: Int) {
        putInt(MONET_SEED_COLOR, color)
    }

    fun secondaryColorEnabled(): Boolean {
        return getInt(MONET_SECONDARY_COLOR, Color.WHITE) != Color.WHITE
    }

    fun getSecondaryColorValue(): Int {
        return getInt(MONET_SECONDARY_COLOR, Color.WHITE)
    }

    fun setSecondaryColorValue(color: Int) {
        putInt(MONET_SECONDARY_COLOR, color)
    }

    fun tertiaryColorEnabled(): Boolean {
        return getInt(MONET_TERTIARY_COLOR, Color.WHITE) != Color.WHITE
    }

    fun getTertiaryColorValue(): Int {
        return getInt(MONET_TERTIARY_COLOR, Color.WHITE)
    }

    fun setTertiaryColorValue(color: Int) {
        putInt(MONET_TERTIARY_COLOR, color)
    }

    fun manualColorOverrideEnabled(): Boolean {
        return getBoolean(MANUAL_OVERRIDE_COLORS, false)
    }

    fun setManualColorOverrideEnabled(enabled: Boolean) {
        putBoolean(MANUAL_OVERRIDE_COLORS, enabled)
    }

    fun isColorOverriddenFor(shadeName: String): Boolean {
        return getInt(shadeName, Int.MIN_VALUE) != Int.MIN_VALUE
    }

    fun getOverriddenColorFor(shadeName: String): Int {
        return getInt(shadeName, Color.BLUE)
    }

    fun clearOverriddenColorFor(shadeName: String) {
        clearPref(shadeName)
    }

    fun clearAllOverriddenColors() {
        systemPaletteNames.forEach { palettes ->
            palettes.forEach { shadeName ->
                clearPref(shadeName)
            }
        }
    }

    fun getLastColorAppliedTimestamp(): Long {
        return getLong(MONET_LAST_UPDATED, 0)
    }

    fun updateColorAppliedTimestamp(timestamp: Long = System.currentTimeMillis()) {
        putLong(MONET_LAST_UPDATED, timestamp)
    }

    fun getOriginalStyleName(): String {
        return getString(MONET_STYLE_ORIGINAL_NAME, "TONAL_SPOT")!!
    }

    fun setOriginalStyleName(name: String) {
        putString(MONET_STYLE_ORIGINAL_NAME, name)
    }

    fun clearOriginalStyleName() {
        clearPref(MONET_STYLE_ORIGINAL_NAME)
    }

    fun getWallpaperColorJson(): String? {
        return getString(WALLPAPER_COLOR_LIST, null)
    }

    fun setWallpaperColorJson(json: String) {
        putString(WALLPAPER_COLOR_LIST, json)
    }

    fun setWallpaperColorList(list: ArrayList<Int>) {
        putString(WALLPAPER_COLOR_LIST, GSON.toJson(list))
    }

    fun getWallpaperColorList(): ArrayList<Int> {
        return getWallpaperColorJson()?.let {
            GSON.fromJson(
                it,
                object : TypeToken<ArrayList<Int?>?>() {}.type
            )
        } ?: arrayListOf()
    }

    fun getSelectedFabricatedApps(): HashMap<String, Boolean> {
        val selectedApps = getString(FABRICATED_OVERLAY_FOR_APPS_STATE, null)
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

    fun setSelectedFabricatedApps(selectedApps: HashMap<String, Boolean>) {
        putString(FABRICATED_OVERLAY_FOR_APPS_STATE, GSON.toJson(selectedApps))
    }

    fun showPerAppThemeWarning(): Boolean {
        return getBoolean(SHOW_PER_APP_THEME_WARN, true)
    }

    fun setShowPerAppThemeWarning(show: Boolean) {
        putBoolean(SHOW_PER_APP_THEME_WARN, show)
    }

    fun tintedTextEnabled(): Boolean {
        return getBoolean(TINT_TEXT_COLOR, true)
    }

    fun setTintedTextEnabled(enabled: Boolean) {
        putBoolean(TINT_TEXT_COLOR, enabled)
    }

    fun getAppListFilteringMethod(): Int {
        return getInt(APP_LIST_FILTER_METHOD, AppType.ALL.ordinal)
    }

    fun setAppListFilteringMethod(method: Int) {
        putInt(APP_LIST_FILTER_METHOD, method)
    }

    fun getCustomStyleRepository(): CustomStyleRepository {
        return CustomStyleRepository(AppDatabase.getInstance().customStyleDao())
    }
}