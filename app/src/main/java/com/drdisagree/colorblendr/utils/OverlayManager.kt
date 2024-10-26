package com.drdisagree.colorblendr.utils

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.ColorBlendr.Companion.shizukuConnection
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.utils.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.FabricatedUtil.assignPerAppColorsToOverlay
import com.drdisagree.colorblendr.utils.FabricatedUtil.createDynamicOverlay
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

@Suppress("unused")
object OverlayManager {

    private val TAG: String = OverlayManager::class.java.simpleName
    private var mRootConnection = rootConnection
    private var mShizukuConnection = shizukuConnection
    private val colorNames: Array<Array<String>> = ColorUtil.colorNames

    fun enableOverlay(packageName: String) {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to enable overlay: $packageName", e
            )
        }
    }

    fun disableOverlay(packageName: String) {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to disable overlay: $packageName", e
            )
        }
    }

    fun isOverlayInstalled(packageName: String): Boolean {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to check if overlay is installed: $packageName", e
            )
            return false
        }
    }

    fun isOverlayEnabled(packageName: String): Boolean {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to check if overlay is enabled: $packageName", e
            )
            return false
        }
    }

    fun uninstallOverlayUpdates(packageName: String) {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to uninstall overlay updates: $packageName", e
            )
        }
    }

    private fun registerFabricatedOverlay(fabricatedOverlay: FabricatedOverlayResource) {
        if (workingMethod != Const.WorkMethod.ROOT) {
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
        if (workingMethod != Const.WorkMethod.ROOT) {
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
            Log.e(
                TAG,
                "Failed to unregister fabricated overlay: $packageName", e
            )
        }
    }

    fun applyFabricatedColors(context: Context) {
        if (!getBoolean(Const.THEMING_ENABLED, true) &&
            !getBoolean(Const.SHIZUKU_THEMING_ENABLED, true)
        ) {
            return
        }

        if (applyFabricatedColorsNonRoot(context)) {
            return
        }

        val style = ColorSchemeUtil.stringToEnumMonetStyle(
            context,
            RPrefs.getString(Const.MONET_STYLE, context.getString(R.string.monet_tonalspot))!!
        )
        val monetAccentSaturation = getInt(Const.MONET_ACCENT_SATURATION, 100)
        val monetBackgroundSaturation = getInt(Const.MONET_BACKGROUND_SATURATION, 100)
        val monetBackgroundLightness = getInt(Const.MONET_BACKGROUND_LIGHTNESS, 100)
        val pitchBlackTheme = getBoolean(Const.MONET_PITCH_BLACK_THEME, false)
        val accurateShades = getBoolean(Const.MONET_ACCURATE_SHADES, true)

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

        val fabricatedOverlays = ArrayList<FabricatedOverlayResource>()
        fabricatedOverlays.add(
            FabricatedOverlayResource(
                Const.FABRICATED_OVERLAY_NAME_SYSTEM,
                Const.FRAMEWORK_PACKAGE
            )
        )

        for (i in 0..4) {
            for (j in 0..12) {
                fabricatedOverlays[0].setColor(
                    colorNames[i][j],
                    paletteDark[i][j]
                )
            }
        }

        fabricatedOverlays[0].createDynamicOverlay(
            paletteLight,
            paletteDark
        )

        // Temporary workaround for Android 15 QPR1 beta 3 background color issue in settings.
        // Currently, we set the status bar color to match the background color
        // to achieve a uniform appearance when the background lightness is reduced.
        // TODO: Remove once the Settings background color issue is resolved.
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Light theme
            fabricatedOverlays[0].setColor(
                "primary_dark_device_default_settings_light", // status bar
                fabricatedOverlays[0].getColor("system_surface_container_light") // background
            )
            // Dark theme
            fabricatedOverlays[0].setColor(
                "primary_dark_device_default_settings", // status bar
                fabricatedOverlays[0].getColor("system_surface_container_dark") // background
            )
        }

        val selectedApps = Const.selectedFabricatedApps

        for (packageName in selectedApps.keys) {
            if (java.lang.Boolean.TRUE == selectedApps[packageName] &&
                SystemUtil.isAppInstalled(packageName)
            ) {
                val fabricatedOverlayPerApp = getFabricatedColorsPerApp(
                    context,
                    packageName,
                    if (SystemUtil.isDarkMode) paletteDark else paletteLight
                )

                fabricatedOverlays.add(fabricatedOverlayPerApp)
            }
        }

        if (pitchBlackTheme) {
            fabricatedOverlays[0].setColor(
                "background_dark",
                Color.BLACK
            )
            fabricatedOverlays[0].setColor(
                "surface_header_dark_sysui",
                Color.BLACK
            ) // QS top part color
            fabricatedOverlays[0].setColor(
                "system_surface_dim_dark",
                Color.BLACK
            ) // A14 notification scrim color
            fabricatedOverlays[0].setColor(
                colorNames[3][11], Color.BLACK
            )
            fabricatedOverlays[0].setColor(
                colorNames[4][11], Color.BLACK
            )
        }

        if (!getBoolean(Const.TINT_TEXT_COLOR, true)) {
            fabricatedOverlays[0].setColor("text_color_primary_device_default_dark", Color.WHITE)
            fabricatedOverlays[0].setColor("text_color_secondary_device_default_dark", -0x4c000001)
            fabricatedOverlays[0].setColor("text_color_primary_device_default_light", Color.BLACK)
            fabricatedOverlays[0].setColor("text_color_secondary_device_default_light", -0x4d000000)
        }

        fabricatedOverlays.add(
            FabricatedOverlayResource(
                Const.FABRICATED_OVERLAY_NAME_SYSTEMUI,
                Const.SYSTEMUI_PACKAGE
            )
        )

        fabricatedOverlays[fabricatedOverlays.size - 1].setBoolean("flag_monet", false)

        for (fabricatedOverlay in fabricatedOverlays) {
            registerFabricatedOverlay(fabricatedOverlay)
        }
    }

    fun applyFabricatedColorsPerApp(
        context: Context,
        packageName: String,
        palette: ArrayList<ArrayList<Int>>?
    ) {
        registerFabricatedOverlay(
            getFabricatedColorsPerApp(
                context,
                packageName,
                palette
            )
        )
    }

    fun removeFabricatedColors(context: Context) {
        if (removeFabricatedColorsNonRoot(context)) {
            return
        }

        val fabricatedOverlays = ArrayList<String>()
        val selectedApps = Const.selectedFabricatedApps

        for (packageName in selectedApps.keys) {
            if (java.lang.Boolean.TRUE == selectedApps[packageName]) {
                fabricatedOverlays.add(
                    String.format(
                        Const.FABRICATED_OVERLAY_NAME_APPS,
                        packageName
                    )
                )
            }
        }

        fabricatedOverlays.add(Const.FABRICATED_OVERLAY_NAME_SYSTEM)
        fabricatedOverlays.add(Const.FABRICATED_OVERLAY_NAME_SYSTEMUI)

        for (packageName in fabricatedOverlays) {
            unregisterFabricatedOverlay(packageName)
        }
    }

    private fun getFabricatedColorsPerApp(
        context: Context,
        packageName: String,
        palette: ArrayList<ArrayList<Int>>?
    ): FabricatedOverlayResource {
        var paletteTemp = palette

        if (paletteTemp == null) {
            paletteTemp = generateModifiedColors(
                ColorSchemeUtil.stringToEnumMonetStyle(
                    context,
                    RPrefs.getString(
                        Const.MONET_STYLE,
                        context.getString(R.string.monet_tonalspot)
                    )!!
                ),
                getInt(Const.MONET_ACCENT_SATURATION, 100),
                getInt(Const.MONET_BACKGROUND_SATURATION, 100),
                getInt(Const.MONET_BACKGROUND_LIGHTNESS, 100),
                getBoolean(Const.MONET_PITCH_BLACK_THEME, false),
                getBoolean(Const.MONET_ACCURATE_SHADES, true),
                modifyPitchBlack = false
            )
        }

        val fabricatedOverlay = FabricatedOverlayResource(
            String.format(Const.FABRICATED_OVERLAY_NAME_APPS, packageName),
            packageName
        )

        fabricatedOverlay.assignPerAppColorsToOverlay(paletteTemp)

        return fabricatedOverlay
    }

    private fun applyFabricatedColorsNonRoot(context: Context): Boolean {
        if (workingMethod != Const.WorkMethod.SHIZUKU) {
            return false
        }

        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission(context)) {
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

    private fun removeFabricatedColorsNonRoot(context: Context): Boolean {
        if (workingMethod != Const.WorkMethod.SHIZUKU) {
            return false
        }

        if (!ShizukuUtil.isShizukuAvailable || !ShizukuUtil.hasShizukuPermission(context)) {
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
