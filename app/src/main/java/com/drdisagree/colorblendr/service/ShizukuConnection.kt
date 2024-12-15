package com.drdisagree.colorblendr.service

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import com.drdisagree.colorblendr.common.Const.COLOR_THEME_APP_ICON
import com.drdisagree.colorblendr.common.Const.LOCK_ADAPTIVE_COLOR
import com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLORS
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLORS_FOR_GOOGLE
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLOR_IS_GRAY
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_STATE
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.topjohnwu.superuser.Shell
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

class ShizukuConnection : IShizukuConnection.Stub {

    companion object {
        private val TAG: String = ShizukuConnection::class.java.simpleName
    }

    @Suppress("unused")
    constructor() {
        Log.i(TAG, "Constructed with no arguments")
    }

    @Suppress("unused")
    @Keep
    constructor(context: Context) {
        Log.i(TAG, "Constructed with context: $context")
    }

    override fun destroy() {
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun applyFabricatedColors(jsonString: String) {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'"
        ).exec()
    }

    override fun applyFabricatedColorsSamsung(paletteArray: String) {
        Shell.cmd(
            "settings put system $LOCK_ADAPTIVE_COLOR '3'",
            "settings put system $WALLPAPER_THEME_COLORS '$paletteArray'",
            "settings put system $WALLPAPER_THEME_COLORS_FOR_GOOGLE '$paletteArray'",
            "settings put system $WALLPAPER_THEME_COLOR_IS_GRAY '0'",
            "settings put system $WALLPAPER_THEME_STATE '1'",
        ).exec()
    }

    override fun applyThemedIconSamsung(isThemed: Boolean) {
        Shell.cmd(
            "settings put system $COLOR_THEME_APP_ICON '${if (isThemed) "1" else "0"}'"
        ).exec()
    }

    override fun removeFabricatedColors() {
        try {
            applyFabricatedColors(originalSettings.toString())
        } catch (e: Exception) {
            Log.e(TAG, "removeFabricatedColors: ", e)
        }
    }

    override fun removeFabricatedColorsSamsung() {
        try {
            Shell.cmd(
                "settings put system $WALLPAPER_THEME_STATE '0'",
                "settings put system $LOCK_ADAPTIVE_COLOR '3'",
                "settings put system $WALLPAPER_THEME_COLORS ''",
                "settings put system $WALLPAPER_THEME_COLORS_FOR_GOOGLE ''"
            ).exec()
        } catch (e: Exception) {
            Log.e(TAG, "removeFabricatedColorsSamsung: ", e)
        }
    }

    override fun getCurrentSettings(): String {
        val currentSettings = Shell.cmd(
            "settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES"
        ).exec().out[0]

        return if (currentSettings == "null") {
            JSONObject().toString()
        } else {
            currentSettings
        }
    }

    @get:Throws(JSONException::class)
    private val originalSettings: JSONObject
        get() {
            return JSONObject(currentSettings).apply {
                val keysToRemove = arrayOf(
                    ThemeOverlayPackage.THEME_STYLE,
                    ThemeOverlayPackage.COLOR_SOURCE,
                    ThemeOverlayPackage.SYSTEM_PALETTE
                )

                for (key in keysToRemove) {
                    remove(key)
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                    remove(ThemeOverlayPackage.ACCENT_COLOR)
                }

                putOpt(ThemeOverlayPackage.COLOR_BOTH, "0")
                putOpt(ThemeOverlayPackage.COLOR_SOURCE, "home_wallpaper")
                putOpt(ThemeOverlayPackage.APPLIED_TIMESTAMP, System.currentTimeMillis())
            }
        }
}
