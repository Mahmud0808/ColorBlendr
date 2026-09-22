package com.drdisagree.colorblendr.utils.manager

import android.graphics.Color
import android.os.RemoteException
import android.util.Log
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.BLISS_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEMUI
import com.drdisagree.colorblendr.data.common.Constant.FRAMEWORK_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
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
import com.drdisagree.colorblendr.data.domain.ThemingErrorReporter
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.utils.app.MiscUtil
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.colors.computeFinalColorOverrides
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.assignFullPaletteToOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.assignPerAppColorsToOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.generateSurfaceEffectColors
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell

@Suppress("unused")
object OverlayManager {

    private const val TAG = "OverlayManager"
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

    private fun shizukuConnectionError(): String? {
        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            return appContext.getString(R.string.error_shizuku_unavailable)
        }

        val connection = ShizukuConnectionProvider.connect()
        if (connection == null) {
            Log.w(TAG, "Shizuku service connection is null")
            return appContext.getString(R.string.error_shizuku_service_unavailable)
        }

        mShizukuConnection = connection
        return null
    }

    fun enableOverlay(packageName: String) {
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.enableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to enable overlay: $packageName", e)
            reportError(overlayError(e))
        }
    }

    fun disableOverlay(packageName: String) {
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.disableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to disable overlay: $packageName", e)
            reportError(overlayError(e))
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
        if (!isRootMode()) return
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return
        }

        try {
            mRootConnection.uninstallOverlayUpdates(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to uninstall overlay updates: $packageName", e)
            reportError(overlayError(e))
        }
    }

    private fun registerFabricatedOverlay(fabricatedOverlay: FabricatedOverlayResource): Boolean {
        if (!isRootMode()) return false
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return false
        }

        return try {
            mRootConnection.registerFabricatedOverlay(fabricatedOverlay)
            mRootConnection.enableOverlayWithIdentifier(listOf(fabricatedOverlay.overlayName))
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register fabricated overlay: " + fabricatedOverlay.overlayName, e)
            reportError(overlayError(e))
            false
        }
    }

    fun unregisterFabricatedOverlay(packageName: String): Boolean {
        if (!isRootMode()) return false
        if (!ensureRootConnection()) {
            reportError(appContext.getString(R.string.error_root_unavailable))
            return false
        }

        return try {
            mRootConnection.unregisterFabricatedOverlay(packageName)
            true
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister fabricated overlay: $packageName", e)
            reportError(overlayError(e))
            false
        }
    }

    fun applyFabricatedColors(): Boolean {
        if (!isThemingEnabled() && !isShizukuThemingEnabled() && !isWirelessAdbThemingEnabled()) return true

        applyFabricatedColorsNonRoot()?.let { return it }

        val style = getCurrentMonetStyle()
        val monetAccentSaturation = getAccentSaturation()
        val monetBackgroundSaturation = getBackgroundSaturation()
        val monetBackgroundLightness = getBackgroundLightness()
        val pitchBlackTheme = pitchBlackThemeEnabled()
        val accurateShades = accurateShadesEnabled()
        val isDarkMode = SystemUtil.isDarkMode

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

        val success = ArrayList<FabricatedOverlayResource>().apply {
            add(
                FabricatedOverlayResource(
                    FABRICATED_OVERLAY_NAME_SYSTEM,
                    FRAMEWORK_PACKAGE
                ).also { frameworkOverlay ->
                    frameworkOverlay.apply {
                        for (i in systemPaletteNames.indices) {
                            for (j in systemPaletteNames[i].indices) {
                                setColor(
                                    systemPaletteNames[i][j],
                                    if (isDarkMode) paletteDark[i][j] else paletteLight[i][j]
                                )
                            }
                        }

                        // Mirror the full Material 3 palette
                        assignFullPaletteToOverlay(paletteLight, paletteDark, isDarkMode)
                        // SurfaceEffectColors
                        generateSurfaceEffectColors(isDarkMode, true)

                        if (pitchBlackTheme) {
                            setColor("background_dark", Color.BLACK)
                            // QS top part color below A16
                            setColor("surface_header_dark_sysui", Color.BLACK, true)
                            if (isDarkMode) {
                                // QS top part color A16+
                                setColor(
                                    "shade_panel_fg_color",
                                    ColorUtils.setAlphaComponent(Color.BLACK, (0.32f * 255).toInt())
                                ) // with blur
                            }
                            setColor(systemPaletteNames[3][11], Color.BLACK)
                            setColor(systemPaletteNames[4][11], Color.BLACK)
                        }

                        if (!tintedTextEnabled()) {
                            setColor("text_color_primary_device_default_dark", Color.WHITE)
                            setColor("text_color_secondary_device_default_dark", -0x4c000001)
                            setColor("text_color_primary_device_default_light", Color.BLACK)
                            setColor("text_color_secondary_device_default_light", -0x4d000000)
                        }

                        // Error, pitch black and A15 workaround role overrides,
                        // shared with the in-app color preview.
                        computeFinalColorOverrides(
                            paletteLight = paletteLight,
                            paletteDark = paletteDark,
                            currentSurfaceContainerDark = getColor("system_surface_container_dark")
                        ).forEach { (resourceName, colorValue) ->
                            setColor(resourceName, colorValue)
                        }
                    }
                }
            )

            add(
                FabricatedOverlayResource(
                    FABRICATED_OVERLAY_NAME_SYSTEMUI,
                    SYSTEMUI_PACKAGE
                ).also { systemuiOverlay ->
                    systemuiOverlay.apply {
                        setBoolean("flag_monet", false, ifExists = true)

                        // Mirror the full Material 3 palette if resources exist
                        assignFullPaletteToOverlay(paletteLight, paletteDark, isDarkMode)

                        // Discovered /e/OS specific resources
                        setColor("shade_scrim_background_dark", Color.BLACK, true)
                        setColor("notification_ripple_tinted_color", Color.WHITE, true)
                        setColor("system_bar_background_opaque", Color.BLACK, true)

                        if (isDarkMode && pitchBlackTheme) {
                            // QS top part color A16+
                            setColor(
                                "shade_panel_base",
                                ColorUtils.setAlphaComponent(Color.BLACK, (0.32f * 255).toInt()),
                                true
                            ) // with blur
                            setColor("shade_panel_fallback", Color.BLACK, true) // no blur
                            // Notification scrim color A16+
                            setColor(
                                "notification_scrim_base",
                                ColorUtils.setAlphaComponent(Color.BLACK, (0.5f * 255).toInt()),
                                true
                            ) // with blur
                            setColor("notification_scrim_fallback", Color.BLACK, true) // no blur
                        }
                    }
                }
            )

            getSelectedFabricatedApps().filter { (packageName, isSelected) ->
                isSelected == java.lang.Boolean.TRUE && SystemUtil.isAppInstalled(packageName)
            }.forEach { (packageName) ->
                add(getFabricatedColorsPerApp(packageName, paletteLight, paletteDark))
            }
        }.map { registerFabricatedOverlay(it) }.all { it }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()

        return success
    }

    fun applyFabricatedColorsPerApp(
        packageName: String,
        paletteDark: ArrayList<ArrayList<Int>>? = null,
        paletteLight: ArrayList<ArrayList<Int>>? = null
    ) {
        registerFabricatedOverlay(getFabricatedColorsPerApp(packageName, paletteLight, paletteDark))
    }

    fun removeFabricatedColors(): Boolean {
        removeFabricatedColorsNonRoot()?.let { return it }

        return ArrayList<String>().apply {
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
            add(String.format(FABRICATED_OVERLAY_NAME_APPS, BLISS_LAUNCHER))
        }.map { unregisterFabricatedOverlay(it) }.all { it }
    }

    private fun getFabricatedColorsPerApp(
        packageName: String,
        paletteLight: ArrayList<ArrayList<Int>>?,
        paletteDark: ArrayList<ArrayList<Int>>?
    ): FabricatedOverlayResource {
        val paletteLightTemp = paletteLight ?: generateModifiedColors(
            getCurrentMonetStyle(),
            getAccentSaturation(),
            getBackgroundSaturation(),
            getBackgroundLightness(),
            pitchBlackThemeEnabled(),
            accurateShadesEnabled(),
            modifyPitchBlack = false,
            isDark = false
        )
        val paletteDarkTemp = paletteDark ?: generateModifiedColors(
            getCurrentMonetStyle(),
            getAccentSaturation(),
            getBackgroundSaturation(),
            getBackgroundLightness(),
            pitchBlackThemeEnabled(),
            accurateShadesEnabled(),
            modifyPitchBlack = false,
            isDark = true
        )

        return FabricatedOverlayResource(
            String.format(FABRICATED_OVERLAY_NAME_APPS, packageName),
            packageName
        ).also { overlay ->
            overlay.assignPerAppColorsToOverlay(paletteLightTemp, paletteDarkTemp)
        }
    }

    private fun applyFabricatedColorsNonRoot(): Boolean? {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return null

        val themeJson = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()
        val samsungPaletteName = "android:SemWT_G_MonetPalette"
        var success = true

        if (isShizukuMode) {
            shizukuConnectionError()?.let { error ->
                reportError(error)
                return false
            }

            try {
                val currentSettings = mShizukuConnection.currentSettings

                if (themeJson.isNotEmpty()) {
                    val output =
                        mShizukuConnection.run("cmd overlay list | grep \"$samsungPaletteName\"")
                    if (output.contains(samsungPaletteName)) {
                        val isEnabled = output.contains("[x]")
                        mShizukuConnection.run("cmd overlay ${if (isEnabled) "disable" else "enable"} $samsungPaletteName")
                    }

                    // Null on success, shell error message on failure.
                    mShizukuConnection.applyFabricatedColors(
                        MiscUtil.mergeJsonStrings(currentSettings, themeJson)
                    )?.let { error ->
                        Log.e(TAG, "applyFabricatedColorsNonRoot: $error")
                        reportError(
                            appContext.getString(R.string.error_overlay_operation, error)
                        )
                        success = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        } else {
            if (!WifiAdbShell.ensureConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                reportError(appContext.getString(R.string.error_wireless_adb_unavailable))
                return false
            }

            try {
                val current =
                    WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
                if (!current.success) {
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, current.output)
                    )
                    return false
                }

                if (themeJson.isNotEmpty()) {
                    // Grep exit 1 = palette absent; only the output matters.
                    val list =
                        WifiAdbShell.exec("cmd overlay list | grep \"$samsungPaletteName\"")
                    if (list.output.contains(samsungPaletteName)) {
                        val isEnabled = list.output.contains("[x]")
                        WifiAdbShell.exec("cmd overlay ${if (isEnabled) "disable" else "enable"} $samsungPaletteName")
                    }

                    val currentSettings = current.output
                        .takeUnless { it == "null" || it.isEmpty() } ?: "{}"
                    val jsonString = MiscUtil.mergeJsonStrings(currentSettings, themeJson)
                    val put = WifiAdbShell
                        .exec("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'")
                    if (!put.success) {
                        Log.e(TAG, "applyFabricatedColorsNonRoot: ${put.output}")
                        reportError(
                            appContext.getString(R.string.error_overlay_operation, put.output)
                        )
                        success = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "applyFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        }

        // Trigger a refresh to all the viewmodels
        RefreshCoordinator.triggerRefresh()

        return success
    }

    private fun removeFabricatedColorsNonRoot(): Boolean? {
        val isShizukuMode = isShizukuMode()
        val isWirelessAdbMode = isWirelessAdbMode()

        if (!isShizukuMode && !isWirelessAdbMode) return null

        val samsungPaletteName = "android:SemWT_G_MonetPalette"
        var success = true

        if (isShizukuMode) {
            shizukuConnectionError()?.let { error ->
                reportError(error)
                return false
            }

            try {
                if (mShizukuConnection
                        .run("cmd overlay list | grep \"$samsungPaletteName\"")
                        .contains(samsungPaletteName)
                ) {
                    mShizukuConnection.run("cmd overlay disable $samsungPaletteName")
                    mShizukuConnection.run("cmd overlay enable $samsungPaletteName")
                }

                // Null on success, shell error message on failure.
                mShizukuConnection.removeFabricatedColors()?.let { error ->
                    Log.e(TAG, "removeFabricatedColorsNonRoot: $error")
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, error)
                    )
                    success = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        } else {
            if (!WifiAdbShell.ensureConnected()) {
                Log.w(TAG, "Device not connected in wireless ADB mode")
                reportError(appContext.getString(R.string.error_wireless_adb_unavailable))
                return false
            }

            try {
                val current =
                    WifiAdbShell.exec("settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES")
                if (!current.success) {
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, current.output)
                    )
                    return false
                }

                // Grep exit 1 = palette absent; only the output matters.
                val list = WifiAdbShell.exec("cmd overlay list | grep \"$samsungPaletteName\"")
                if (list.output.contains(samsungPaletteName)) {
                    WifiAdbShell.exec("cmd overlay disable $samsungPaletteName")
                    WifiAdbShell.exec("cmd overlay enable $samsungPaletteName")
                }

                val currentSettings = current.output
                    .takeUnless { it == "null" || it.isEmpty() } ?: "{}"
                val jsonString = ThemeOverlayPackage
                    .getOriginalSettings(currentSettings)
                    .toString()
                val put = WifiAdbShell
                    .exec("settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'")
                if (!put.success) {
                    Log.e(TAG, "removeFabricatedColorsNonRoot: ${put.output}")
                    reportError(
                        appContext.getString(R.string.error_overlay_operation, put.output)
                    )
                    success = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFabricatedColorsNonRoot: ", e)
                reportError(overlayError(e))
                success = false
            }
        }

        return success
    }

    private fun reportError(message: String) {
        ThemingErrorReporter.report(message)
    }

    private fun overlayError(e: Exception): String =
        appContext.getString(
            R.string.error_overlay_operation,
            e.message ?: e.javaClass.simpleName
        )
}
