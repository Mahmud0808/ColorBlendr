package com.drdisagree.colorblendr.data.common

import android.app.UiModeManager
import android.graphics.Color
import android.os.Build
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant.APP_COLOR_OVERRIDES
import com.drdisagree.colorblendr.data.common.Constant.APP_LIST_FILTER_METHOD
import com.drdisagree.colorblendr.data.common.Constant.COLOR_SPEC_VERSION
import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.DEVELOPER_MODE
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_FOR_APPS_STATE
import com.drdisagree.colorblendr.data.common.Constant.FIRST_RUN
import com.drdisagree.colorblendr.data.common.Constant.FORCE_PITCH_BLACK_SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.GSON
import com.drdisagree.colorblendr.data.common.Constant.LEGACY_TOKENS
import com.drdisagree.colorblendr.data.common.Constant.MONET_CONTRAST
import com.drdisagree.colorblendr.data.common.Constant.MODE_SPECIFIC_THEMES
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.data.common.Constant.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.data.common.Constant.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.data.common.Constant.MONET_SECONDARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.MONET_STYLE_ORIGINAL_NAME
import com.drdisagree.colorblendr.data.common.Constant.MONET_TERTIARY_COLOR
import com.drdisagree.colorblendr.data.common.Constant.PITCH_BLACK_AUTO_SELECTED_APPS
import com.drdisagree.colorblendr.data.common.Constant.PITCH_BLACK_DECLINED_APPS
import com.drdisagree.colorblendr.data.common.Constant.PREF_WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Constant.SAMSUNG_PALETTE_ORIGINAL_STATE
import com.drdisagree.colorblendr.data.common.Constant.SCREEN_OFF_UPDATE_COLORS
import com.drdisagree.colorblendr.data.common.Constant.SHIZUKU_THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.SHOW_PALETTE_ROOTLESS_HINT
import com.drdisagree.colorblendr.data.common.Constant.SHOW_PALETTE_ROOT_HINT
import com.drdisagree.colorblendr.data.common.Constant.SHOW_PER_APP_THEME_WARN
import com.drdisagree.colorblendr.data.common.Constant.TASKER_INTEGRATION
import com.drdisagree.colorblendr.data.common.Constant.THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.TINT_TEXT_COLOR
import com.drdisagree.colorblendr.data.common.Constant.TOKEN_OVERRIDES
import com.drdisagree.colorblendr.data.common.Constant.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.data.common.Constant.WIRELESS_ADB_THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Constant.accentSaturationKey
import com.drdisagree.colorblendr.data.common.Constant.backgroundLightnessKey
import com.drdisagree.colorblendr.data.common.Constant.backgroundSaturationKey
import com.drdisagree.colorblendr.data.config.Prefs.clearPref
import com.drdisagree.colorblendr.data.config.Prefs.getBoolean
import com.drdisagree.colorblendr.data.config.Prefs.getCommittedBoolean
import com.drdisagree.colorblendr.data.config.Prefs.getInt
import com.drdisagree.colorblendr.data.config.Prefs.getLong
import com.drdisagree.colorblendr.data.config.Prefs.getString
import com.drdisagree.colorblendr.data.config.Prefs.putBoolean
import com.drdisagree.colorblendr.data.config.Prefs.putInt
import com.drdisagree.colorblendr.data.config.Prefs.putLong
import com.drdisagree.colorblendr.data.config.Prefs.putString
import com.drdisagree.colorblendr.data.database.AppDatabase
import com.drdisagree.colorblendr.data.enums.AppType
import com.drdisagree.colorblendr.data.enums.ContrastLevel
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.enums.MONET.Companion.toEnumMonet
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.data.repository.CommunityThemeRepository
import com.drdisagree.colorblendr.data.repository.CustomStyleRepository
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.override.ColorOverrideCodec
import com.drdisagree.colorblendr.theme.override.ResourceOverrides
import com.drdisagree.colorblendr.theme.override.TokenOverrides
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.google.gson.reflect.TypeToken

@Suppress("unused")
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

    fun getPitchBlackAutoSelectedApps(): Set<String>? =
        getString(PITCH_BLACK_AUTO_SELECTED_APPS, null)
            ?.split(',')
            ?.filter { it.isNotEmpty() }
            ?.toSet()

    fun setPitchBlackAutoSelectedApps(packages: Set<String>?) {
        if (packages == null) {
            clearPref(PITCH_BLACK_AUTO_SELECTED_APPS)
        } else {
            putString(PITCH_BLACK_AUTO_SELECTED_APPS, packages.joinToString(","))
        }
    }

    fun getPitchBlackDeclinedApps(): Set<String> = getString(PITCH_BLACK_DECLINED_APPS, null)
        ?.split(',')
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()

    fun setPitchBlackDeclinedApps(packages: Set<String>) {
        if (packages.isEmpty()) {
            clearPref(PITCH_BLACK_DECLINED_APPS)
        } else {
            putString(PITCH_BLACK_DECLINED_APPS, packages.joinToString(","))
        }
    }

    fun getSamsungPaletteOriginalState(): Boolean? {
        return when (getInt(SAMSUNG_PALETTE_ORIGINAL_STATE, -1)) {
            0 -> false
            1 -> true
            else -> null
        }
    }

    fun setSamsungPaletteOriginalState(enabled: Boolean?) {
        if (enabled == null) {
            clearPref(SAMSUNG_PALETTE_ORIGINAL_STATE)
        } else {
            putInt(SAMSUNG_PALETTE_ORIGINAL_STATE, if (enabled) 1 else 0)
        }
    }

    fun isShizukuThemingEnabled(defaultValue: Boolean = true): Boolean {
        return getBoolean(SHIZUKU_THEMING_ENABLED, defaultValue)
    }

    fun setShizukuThemingEnabled(enabled: Boolean) {
        putBoolean(SHIZUKU_THEMING_ENABLED, enabled)
    }

    fun isWirelessAdbThemingEnabled(defaultValue: Boolean = true): Boolean {
        return getBoolean(WIRELESS_ADB_THEMING_ENABLED, defaultValue)
    }

    fun setWirelessAdbThemingEnabled(enabled: Boolean) {
        putBoolean(WIRELESS_ADB_THEMING_ENABLED, enabled)
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

    fun isWirelessAdbMode(): Boolean {
        return getWorkingMethod() == WorkMethod.WIRELESS_ADB
    }

    fun isWorkMethodUnknown(): Boolean {
        return getWorkingMethod() == WorkMethod.NULL
    }

    fun getCurrentMonetStyle(): MONET {
        return getString(MONET_STYLE, MONET.TONAL_SPOT.toString()).toEnumMonet()
    }

    fun setCurrentMonetStyle(monet: MONET) {
        putString(MONET_STYLE, monet.toString())
    }

    fun canonicalMonetStyle(raw: String?): String = raw.toEnumMonet().toString()

    fun normalizeMonetStyle() {
        val stored = getString(MONET_STYLE, null) ?: return
        val canonical = canonicalMonetStyle(stored)
        if (stored != canonical) putString(MONET_STYLE, canonical)
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

    fun taskerIntegrationEnabled(): Boolean {
        return getBoolean(TASKER_INTEGRATION, false)
    }

    fun setTaskerIntegrationEnabled(enabled: Boolean) {
        putBoolean(TASKER_INTEGRATION, enabled)
    }

    fun screenOffColorUpdateEnabled(): Boolean {
        return getBoolean(SCREEN_OFF_UPDATE_COLORS, false)
    }

    fun setScreenOffColorUpdateEnabled(enabled: Boolean) {
        putBoolean(SCREEN_OFF_UPDATE_COLORS, enabled)
    }

    fun getColorSpecVersion(): Int {
        return getInt(COLOR_SPEC_VERSION, 0)
    }

    fun setColorSpecVersion(version: Int) {
        putInt(COLOR_SPEC_VERSION, version)
    }

    fun forcePitchBlackSettingsEnabled(): Boolean {
        return getBoolean(FORCE_PITCH_BLACK_SETTINGS, false)
    }

    fun setForcePitchBlackSettingsEnabled(enabled: Boolean) {
        putBoolean(FORCE_PITCH_BLACK_SETTINGS, enabled)
    }

    fun getAccentSaturation(isDark: Boolean = SystemUtil.isDarkMode): Int {
        return getInt(accentSaturationKey(isDark), 100)
    }

    fun setAccentSaturation(value: Int, isDark: Boolean = SystemUtil.isDarkMode) {
        putInt(accentSaturationKey(isDark), value)
    }

    fun resetAccentSaturation(isDark: Boolean = SystemUtil.isDarkMode) {
        clearPref(accentSaturationKey(isDark))
    }

    fun getBackgroundSaturation(isDark: Boolean = SystemUtil.isDarkMode): Int {
        return getInt(backgroundSaturationKey(isDark), 100)
    }

    fun setBackgroundSaturation(value: Int, isDark: Boolean = SystemUtil.isDarkMode) {
        putInt(backgroundSaturationKey(isDark), value)
    }

    fun resetBackgroundSaturation(isDark: Boolean = SystemUtil.isDarkMode) {
        clearPref(backgroundSaturationKey(isDark))
    }

    fun getBackgroundLightness(isDark: Boolean = SystemUtil.isDarkMode): Int {
        return getInt(backgroundLightnessKey(isDark), 100)
    }

    fun setBackgroundLightness(value: Int, isDark: Boolean = SystemUtil.isDarkMode) {
        putInt(backgroundLightnessKey(isDark), value)
    }

    fun resetBackgroundLightness(isDark: Boolean = SystemUtil.isDarkMode) {
        clearPref(backgroundLightnessKey(isDark))
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

    fun isColorOverriddenFor(shadeName: String): Boolean {
        return getInt(shadeName, Int.MIN_VALUE) != Int.MIN_VALUE
    }

    fun getOverriddenColorFor(shadeName: String): Int {
        return getInt(shadeName, Color.BLUE)
    }

    fun clearOverriddenColorFor(shadeName: String) {
        clearPref(shadeName)
    }

    fun hasOverriddenShades(): Boolean {
        return PaletteRole.shadeNames.any { palettes -> palettes.any(::isColorOverriddenFor) }
    }

    fun clearAllOverriddenShades() {
        PaletteRole.shadeNames.forEach { palettes ->
            palettes.forEach { shadeName ->
                clearPref(shadeName)
            }
        }
    }

    fun clearAllOverriddenColors() {
        clearAllOverriddenShades()
        clearPref(TOKEN_OVERRIDES)
    }

    fun getTokenOverrides(): TokenOverrides {
        return ColorOverrideCodec.decodeTokens(getString(TOKEN_OVERRIDES))
    }

    fun legacyTokensEnabled(): Boolean {
        return getBoolean(LEGACY_TOKENS, false)
    }

    fun legacyTokensApplied(): Boolean = getCommittedBoolean(LEGACY_TOKENS, false)

    fun setLegacyTokensEnabled(enabled: Boolean) {
        if (enabled) putBoolean(LEGACY_TOKENS, true) else clearPref(LEGACY_TOKENS)
    }

    fun getContrastLevel(): ContrastLevel =
        ContrastLevel.fromString(getString(MONET_CONTRAST, ContrastLevel.STANDARD.name))

    fun setContrastLevel(level: ContrastLevel) {
        putString(MONET_CONTRAST, level.name)
    }

    fun contrastValue(): Double =
        if (isRootMode()) getContrastLevel().value else systemContrast()

    private fun systemContrast(): Double =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            appContext.getSystemService(UiModeManager::class.java)?.contrast?.toDouble() ?: 0.0
        } else {
            0.0
        }

    fun getAppColorOverrides(): Map<String, ResourceOverrides> {
        return ColorOverrideCodec.decodeApps(getString(APP_COLOR_OVERRIDES))
    }

    fun getAppColorOverrides(packageName: String): ResourceOverrides {
        return getAppColorOverrides()[packageName] ?: ResourceOverrides()
    }

    fun setAppColorOverrides(packageName: String, overrides: ResourceOverrides) {
        val all = getAppColorOverrides().toMutableMap().apply {
            if (overrides.isEmpty) remove(packageName) else put(packageName, overrides)
        }
        if (all.isEmpty()) {
            clearPref(APP_COLOR_OVERRIDES)
        } else {
            putString(APP_COLOR_OVERRIDES, ColorOverrideCodec.encodeApps(all))
        }
    }

    fun setTokenOverrides(overrides: TokenOverrides) {
        if (overrides.isEmpty) {
            clearPref(TOKEN_OVERRIDES)
        } else {
            putString(TOKEN_OVERRIDES, ColorOverrideCodec.encodeTokens(overrides))
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

    fun showColorPaletteHint(rootMode: Boolean): Boolean {
        return getBoolean(paletteHintKey(rootMode), true)
    }

    fun setShowColorPaletteHint(rootMode: Boolean, show: Boolean) {
        putBoolean(paletteHintKey(rootMode), show)
    }

    private fun paletteHintKey(rootMode: Boolean): String =
        if (rootMode) SHOW_PALETTE_ROOT_HINT else SHOW_PALETTE_ROOTLESS_HINT

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
        return CustomStyleRepository { AppDatabase.getInstance().customStyleDao() }
    }

    fun developerModeEnabled(): Boolean {
        return getBoolean(DEVELOPER_MODE, false)
    }

    fun setDeveloperModeEnabled(enabled: Boolean) {
        putBoolean(DEVELOPER_MODE, enabled)
    }

    fun getCommunityThemeRepository(): CommunityThemeRepository {
        return CommunityThemeRepository { AppDatabase.getInstance().communityThemeDao() }
    }
}