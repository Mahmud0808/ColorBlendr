package com.drdisagree.colorblendr.utils

import android.content.Context
import android.graphics.Color
import android.os.RemoteException
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.rootConnection
import com.drdisagree.colorblendr.ColorBlendr.Companion.shizukuConnection
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.drdisagree.colorblendr.utils.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource

@Suppress("unused")
object OverlayManager {

    private val TAG: String = OverlayManager::class.java.simpleName
    private var mRootConnection = rootConnection
    private var mShizukuConnection = shizukuConnection
    private val colorNames: Array<Array<String>> = ColorUtil.colorNames

    fun enableOverlay(packageName: String) {
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
        if (Const.workingMethod != Const.WorkMethod.ROOT) {
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
        if (Const.workingMethod != Const.WorkMethod.ROOT) {
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

    suspend fun applyFabricatedColors(context: Context) {
        if (!RPrefs.getBoolean(
                Const.THEMING_ENABLED,
                true
            ) && !RPrefs.getBoolean(Const.SHIZUKU_THEMING_ENABLED, true)
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
        val monetAccentSaturation = RPrefs.getInt(Const.MONET_ACCENT_SATURATION, 100)
        val monetBackgroundSaturation = RPrefs.getInt(Const.MONET_BACKGROUND_SATURATION, 100)
        val monetBackgroundLightness = RPrefs.getInt(Const.MONET_BACKGROUND_LIGHTNESS, 100)
        val pitchBlackTheme = RPrefs.getBoolean(Const.MONET_PITCH_BLACK_THEME, false)
        val accurateShades = RPrefs.getBoolean(Const.MONET_ACCURATE_SHADES, true)

        val paletteLight = ColorUtil.generateModifiedColors(
            style = style,
            accentSaturation = monetAccentSaturation,
            backgroundSaturation = monetBackgroundSaturation,
            backgroundLightness = monetBackgroundLightness,
            pitchBlackTheme = pitchBlackTheme,
            accurateShades = accurateShades,
            modifyPitchBlack = false,
            isDark = false
        )

        val paletteDark = ColorUtil.generateModifiedColors(
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

        FabricatedUtil.createDynamicOverlay(
            fabricatedOverlays[0],
            paletteLight,
            paletteDark
        )

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

        fabricatedOverlays[0].setColor(
            "system_surface_dim_dark",
            fabricatedOverlays[0].getColor(
                colorNames[3][11]
            ) // system_neutral1_900
        ) // A14 notification scrim color
        fabricatedOverlays[0].setColor(
            "system_surface_container_high_dark",
            fabricatedOverlays[0].getColor(
                colorNames[3][10]
            ) // system_neutral1_800
        ) // A14 notification background color

        if (pitchBlackTheme) {
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

        if (!RPrefs.getBoolean(Const.TINT_TEXT_COLOR, true)) {
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

    suspend fun applyFabricatedColorsPerApp(
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

    private suspend fun getFabricatedColorsPerApp(
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
                RPrefs.getInt(Const.MONET_ACCENT_SATURATION, 100),
                RPrefs.getInt(Const.MONET_BACKGROUND_SATURATION, 100),
                RPrefs.getInt(Const.MONET_BACKGROUND_LIGHTNESS, 100),
                RPrefs.getBoolean(Const.MONET_PITCH_BLACK_THEME, false),
                RPrefs.getBoolean(Const.MONET_ACCURATE_SHADES, true),
                modifyPitchBlack = false
            )
        }

        val fabricatedOverlay = FabricatedOverlayResource(
            String.format(Const.FABRICATED_OVERLAY_NAME_APPS, packageName),
            packageName
        )

        FabricatedUtil.assignPerAppColorsToOverlay(fabricatedOverlay, paletteTemp)

        return fabricatedOverlay
    }

    private fun applyFabricatedColorsNonRoot(context: Context): Boolean {
        if (Const.workingMethod != Const.WorkMethod.SHIZUKU) {
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
        if (Const.workingMethod != Const.WorkMethod.SHIZUKU) {
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
