package com.drdisagree.colorblendr.utils.manager

import android.graphics.Color
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.ColorBlendr.Companion.shizukuConnection
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEMUI
import com.drdisagree.colorblendr.data.common.Constant.FRAMEWORK_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.forcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbMode
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.utils.app.MiscUtil
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.adjustLightness
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.assignPerAppColorsToOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.createDynamicOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.generateSurfaceEffectColors
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil

@Suppress("unused")
object OverlayManager {

    private val TAG: String = OverlayManager::class.java.simpleName
    private lateinit var mRootConnection: IRootConnection
    private lateinit var mShizukuConnection: IShizukuConnection

    private fun ensureRootConnection(): Boolean {
        if (::mRootConnection.isInitialized.not()) {
            if (rootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return false
            }
            mRootConnection = rootConnection!!
        }
        return true
    }

    private fun ensureShizukuConnection(): Boolean {
        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            return false
        } else if (::mShizukuConnection.isInitialized.not()) {
            if (shizukuConnection == null) {
                Log.w(TAG, "Shizuku service connection is null")
                return false
            }
            mShizukuConnection = shizukuConnection!!
        }
        return true
    }

    fun enableOverlay(packageName: String) {
        if (!isRootMode() || !ensureRootConnection()) return

        try {
            mRootConnection.enableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to enable overlay: $packageName", e)
        }
    }

    fun disableOverlay(packageName: String) {
        if (!isRootMode() || !ensureRootConnection()) return

        try {
            mRootConnection.disableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to disable overlay: $packageName", e)
        }
    }

    fun isOverlayInstalled(packageName: String): Boolean {
        if (!isRootMode() || !ensureRootConnection()) return false

        try {
            return mRootConnection.isOverlayInstalled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is installed: $packageName", e)
            return false
        }
    }

    fun isOverlayEnabled(packageName: String): Boolean {
        if (!isRootMode() || !ensureRootConnection()) return false

        try {
            return mRootConnection.isOverlayEnabled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is enabled: $packageName", e)
            return false
        }
    }

    fun uninstallOverlayUpdates(packageName: String) {
        if (!isRootMode() || !ensureRootConnection()) return

        try {
            mRootConnection.uninstallOverlayUpdates(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to uninstall overlay updates: $packageName", e)
        }
    }

    private fun registerFabricatedOverlay(fabricatedOverlay: FabricatedOverlayResource) {
        if (!isRootMode() || !ensureRootConnection()) return

        try {
            mRootConnection.registerFabricatedOverlay(fabricatedOverlay)
            mRootConnection.enableOverlayWithIdentifier(listOf(fabricatedOverlay.overlayName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register fabricated overlay: " + fabricatedOverlay.overlayName, e)
        }
    }

    fun unregisterFabricatedOverlay(packageName: String) {
        if (!isRootMode() || !ensureRootConnection()) return

        try {
            mRootConnection.unregisterFabricatedOverlay(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister fabricated overlay: $packageName", e)
        }
    }

    fun applyFabricatedColors() {
        if (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled()) return

        if (applyFabricatedColorsNonRoot()) return

        val style = getCurrentMonetStyle()
        val monetAccentSaturation = getAccentSaturation()
        val monetBackgroundSaturation = getBackgroundSaturation()
        val monetBackgroundLightness = getBackgroundLightness()
        val pitchBlackTheme = pitchBlackThemeEnabled()
        val accurateShades = accurateShadesEnabled()

        val paletteLight = generateModifiedColors(
            style = style,
            accentSaturation = monetAccentSaturation,
            backgroundSaturation = monetBackgroundSaturation,
            backgroundLightness = monetBackgroundLightness,
            pitchBlackTheme = pitchBlackTheme,
            accurateShades = accurateShades,
            modifyPitchBlack = false,
            isDark = false
        )

        val paletteDark = generateModifiedColors(
            style = style,
            accentSaturation = monetAccentSaturation,
            backgroundSaturation = monetBackgroundSaturation,
            backgroundLightness = monetBackgroundLightness,
            pitchBlackTheme = pitchBlackTheme,
            accurateShades = accurateShades,
            modifyPitchBlack = false,
            isDark = true
        )

        ArrayList<FabricatedOverlayResource>().apply {
            add(
                FabricatedOverlayResource(
                    FABRICATED_OVERLAY_NAME_SYSTEM,
                    FRAMEWORK_PACKAGE
                ).also { frameworkOverlay ->
                    val isDarkMode = SystemUtil.isDarkMode

                    frameworkOverlay.apply {
                        for (i in systemPaletteNames.indices) {
                            for (j in systemPaletteNames[i].indices) {
                                setColor(
                                    systemPaletteNames[i][j],
                                    if (isDarkMode) paletteDark[i][j] else paletteLight[i][j]
                                )
                            }
                        }
                        // SurfaceEffectColors
                        // Source: https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/packages/SystemUI/src/com/android/systemui/common/shared/colors/SurfaceEffectColors.kt
                        generateSurfaceEffectColors(isDarkMode)

                        // Dynamic colors
                        createDynamicOverlay(paletteLight, paletteDark)

                        // Temporary workaround for Android 15 QPR1 beta 3 background color issue in settings.
                        // Currently, we set the status bar color to match the background color
                        // to achieve a uniform appearance when the background lightness is reduced.
                        // TODO: Remove once the Settings background color issue is resolved.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
                            && pitchBlackTheme && isDarkMode && forcePitchBlackSettingsEnabled()
                        ) {
                            setColor(
                                "system_surface_container_dark",
                                adjustLightness(getColor("system_surface_container_dark"), -58)
                            )
                        }

                        if (pitchBlackTheme) {
                            setColor("background_dark", Color.BLACK)
                            // QS top part color
                            setColor("surface_header_dark_sysui", Color.BLACK)
                            // A14 notification scrim color
                            setColor("system_surface_dim_dark", Color.BLACK)
                            setColor(systemPaletteNames[3][11], Color.BLACK)
                            setColor(systemPaletteNames[4][11], Color.BLACK)
                        }

                        if (!tintedTextEnabled()) {
                            setColor("text_color_primary_device_default_dark", Color.WHITE)
                            setColor("text_color_secondary_device_default_dark", -0x4c000001)
                            setColor("text_color_primary_device_default_light", Color.BLACK)
                            setColor("text_color_secondary_device_default_light", -0x4d000000)
                        }
                    }
                }
            )

            add(
                FabricatedOverlayResource(
                    FABRICATED_OVERLAY_NAME_SYSTEMUI,
                    SYSTEMUI_PACKAGE
                ).also { systemuiOverlay ->
                    systemuiOverlay.setBoolean("flag_monet", false)
                }
            )

            getSelectedFabricatedApps().filter { (packageName, isSelected) ->
                isSelected == java.lang.Boolean.TRUE && SystemUtil.isAppInstalled(packageName)
            }.forEach { (packageName) ->
                add(
                    getFabricatedColorsPerApp(
                        packageName,
                        if (SystemUtil.isDarkMode) paletteDark else paletteLight
                    )
                )
            }
        }.forEach { registerFabricatedOverlay(it) }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()
    }

    fun applyFabricatedColorsPerApp(
        packageName: String,
        palette: ArrayList<ArrayList<Int>>?
    ) {
        registerFabricatedOverlay(
            getFabricatedColorsPerApp(
                packageName,
                palette
            )
        )
    }

    fun removeFabricatedColors() {
        if (removeFabricatedColorsNonRoot()) return

        ArrayList<String>().apply {
            getSelectedFabricatedApps().filter { (_, isSelected) ->
                isSelected == java.lang.Boolean.TRUE
            }.forEach { (packageName) ->
                add(
                    String.format(
                        FABRICATED_OVERLAY_NAME_APPS,
                        packageName
                    )
                )
            }

            add(FABRICATED_OVERLAY_NAME_SYSTEM)
            add(FABRICATED_OVERLAY_NAME_SYSTEMUI)
        }.forEach { unregisterFabricatedOverlay(it) }
    }

    private fun getFabricatedColorsPerApp(
        packageName: String,
        palette: ArrayList<ArrayList<Int>>?
    ): FabricatedOverlayResource {
        var paletteTemp = palette

        if (paletteTemp == null) {
            paletteTemp = generateModifiedColors(
                getCurrentMonetStyle(),
                getAccentSaturation(),
                getBackgroundSaturation(),
                getBackgroundLightness(),
                pitchBlackThemeEnabled(),
                accurateShadesEnabled(),
                modifyPitchBlack = false
            )
        }

        return FabricatedOverlayResource(
            String.format(FABRICATED_OVERLAY_NAME_APPS, packageName),
            packageName
        ).also { overlay ->
            overlay.assignPerAppColorsToOverlay(paletteTemp)
        }
    }

    private fun applyFabricatedColorsNonRoot(): Boolean {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return false

        val themeJson = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()

        if (isShizukuMode) {
            if (!ensureShizukuConnection()) return true

            try {
                val currentSettings = mShizukuConnection.currentSettings

                if (themeJson.isNotEmpty()) {
                    mShizukuConnection.applyFabricatedColors(
                        MiscUtil.mergeJsonStrings(currentSettings, themeJson)
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "applyFabricatedColorsNonRoot: ", e)
            }
        } else {
            if (!WifiAdbShell.isMyDeviceConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                return true
            }

            try {
                WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES") { currentSettings ->
                    if (themeJson.isNotEmpty()) {
                        val jsonString = MiscUtil.mergeJsonStrings(
                            currentSettings,
                            themeJson
                        )
                        WifiAdbShell.execute("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "applyFabricatedColorsNonRoot: ", e)
            }
        }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()

        return true
    }

    private fun removeFabricatedColorsNonRoot(): Boolean {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return false

        if (isShizukuMode) {
            if (!ensureShizukuConnection()) return true

            try {
                mShizukuConnection.removeFabricatedColors()
            } catch (e: Exception) {
                Log.d(TAG, "removeFabricatedColorsNonRoot: ", e)
            }
        } else {
            if (!WifiAdbShell.isMyDeviceConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                return true
            }

            try {
                WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES") { currentSettings ->
                    val jsonString = ThemeOverlayPackage
                        .getOriginalSettings(currentSettings.ifEmpty { "{}" })
                        .toString()
                    WifiAdbShell.execute("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'")
                }
            } catch (e: Exception) {
                Log.d(TAG, "applyFabricatedColorsNonRoot: ", e)
            }
        }

        return true
    }
}
