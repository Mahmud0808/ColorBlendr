package com.drdisagree.colorblendr.utils

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
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.utils.ColorUtil.adjustLightness
import com.drdisagree.colorblendr.utils.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.FabricatedUtil.assignPerAppColorsToOverlay
import com.drdisagree.colorblendr.utils.FabricatedUtil.createDynamicOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

@Suppress("unused")
object OverlayManager {

    private val TAG: String = OverlayManager::class.java.simpleName
    private var mRootConnection = rootConnection
    private var mShizukuConnection = shizukuConnection

    fun enableOverlay(packageName: String) {
        if (!isRootMode()) {
            return
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return
            }
        }

        try {
            mRootConnection!!.enableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to enable overlay: $packageName", e)
        }
    }

    fun disableOverlay(packageName: String) {
        if (!isRootMode()) {
            return
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return
            }
        }

        try {
            mRootConnection!!.disableOverlay(listOf(packageName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to disable overlay: $packageName", e)
        }
    }

    fun isOverlayInstalled(packageName: String): Boolean {
        if (!isRootMode()) {
            return false
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return false
            }
        }

        try {
            return mRootConnection!!.isOverlayInstalled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is installed: $packageName", e)
            return false
        }
    }

    fun isOverlayEnabled(packageName: String): Boolean {
        if (!isRootMode()) {
            return false
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return false
            }
        }

        try {
            return mRootConnection!!.isOverlayEnabled(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to check if overlay is enabled: $packageName", e)
            return false
        }
    }

    fun uninstallOverlayUpdates(packageName: String) {
        if (!isRootMode()) {
            return
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return
            }
        }

        try {
            mRootConnection!!.uninstallOverlayUpdates(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to uninstall overlay updates: $packageName", e)
        }
    }

    private fun registerFabricatedOverlay(fabricatedOverlay: FabricatedOverlayResource) {
        if (!isRootMode()) {
            return
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return
            }
        }

        try {
            mRootConnection!!.registerFabricatedOverlay(fabricatedOverlay)
            mRootConnection!!.enableOverlayWithIdentifier(listOf(fabricatedOverlay.overlayName))
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to register fabricated overlay: " + fabricatedOverlay.overlayName, e)
        }
    }

    fun unregisterFabricatedOverlay(packageName: String) {
        if (!isRootMode()) {
            return
        }

        if (mRootConnection == null) {
            mRootConnection = rootConnection

            if (mRootConnection == null) {
                Log.w(TAG, "Root service connection is null")
                return
            }
        }

        try {
            mRootConnection!!.unregisterFabricatedOverlay(packageName)
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to unregister fabricated overlay: $packageName", e)
        }
    }

    fun applyFabricatedColors() {
        if (!isThemingEnabled() && !isShizukuThemingEnabled()) {
            return
        }

        if (applyFabricatedColorsNonRoot()) {
            return
        }

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

                        createDynamicOverlay(
                            paletteLight,
                            paletteDark
                        )

                        // Temporary workaround for Android 15 QPR1 beta 3 background color issue in settings.
                        // Currently, we set the status bar color to match the background color
                        // to achieve a uniform appearance when the background lightness is reduced.
                        // TODO: Remove once the Settings background color issue is resolved.
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            // Pitch black settings workaround
                            if (pitchBlackTheme && isDarkMode && forcePitchBlackSettingsEnabled()) {
                                setColor(
                                    "system_surface_container_dark",
                                    adjustLightness(
                                        getColor("system_surface_container_dark"),
                                        -58
                                    )
                                )
                            }
                            // Light theme
                            setColor(
                                "primary_dark_device_default_settings_light", // status bar
                                getColor("system_surface_container_light") // background
                            )
                            // Dark theme
                            setColor(
                                "primary_dark_device_default_settings", // status bar
                                getColor("system_surface_container_dark") // background
                            )
                        }

                        if (pitchBlackTheme) {
                            setColor("background_dark", Color.BLACK)
                            setColor("surface_header_dark_sysui", Color.BLACK) // QS top part color
                            setColor(
                                "system_surface_dim_dark",
                                Color.BLACK
                            ) // A14 notification scrim color
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
        if (removeFabricatedColorsNonRoot()) {
            return
        }

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
        if (!isShizukuMode()) {
            return false
        }

        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            return true
        }

        if (mShizukuConnection == null) {
            mShizukuConnection = shizukuConnection

            if (mShizukuConnection == null) {
                Log.w(TAG, "Shizuku service connection is null")
                return true
            }
        }

        try {
            val currentSettings = mShizukuConnection!!.currentSettings
            val jsonString = ThemeOverlayPackage.themeCustomizationOverlayPackages.toString()

            if (jsonString.isNotEmpty()) {
                mShizukuConnection!!.applyFabricatedColors(
                    MiscUtil.mergeJsonStrings(currentSettings, jsonString)
                )
            }
        } catch (e: Exception) {
            Log.d(TAG, "applyFabricatedColorsNonRoot: ", e)
        }

        return true
    }

    private fun removeFabricatedColorsNonRoot(): Boolean {
        if (!isShizukuMode()) {
            return false
        }

        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available")
            return true
        }

        if (mShizukuConnection == null) {
            mShizukuConnection = shizukuConnection

            if (mShizukuConnection == null) {
                Log.w(TAG, "Shizuku service connection is null")
                return true
            }
        }

        try {
            mShizukuConnection!!.removeFabricatedColors()
        } catch (e: Exception) {
            Log.d(TAG, "removeFabricatedColorsNonRoot: ", e)
        }

        return true
    }
}
