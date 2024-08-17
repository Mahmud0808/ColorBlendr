package com.drdisagree.colorblendr.service

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.topjohnwu.superuser.Shell
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

class ShizukuConnection : IShizukuConnection.Stub {
    constructor() {
        Log.i(TAG, "Constructed with no arguments")
    }

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
        val mCommand =
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'"
        Shell.cmd(mCommand).exec()
    }

    override fun removeFabricatedColors() {
        try {
            applyFabricatedColors(originalSettings.toString())
        } catch (e: Exception) {
            Log.e(TAG, "removeFabricatedColors: ", e)
        }
    }

    override fun getCurrentSettings(): String {
        val mCommand = "settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES"
        return Shell.cmd(mCommand).exec().out[0]
    }

    @get:Throws(JSONException::class)
    private val originalSettings: JSONObject
        get() {
            val currentSettings = currentSettings
            val jsonObject = JSONObject(currentSettings)

            val keysToRemove = arrayOf(
                ThemeOverlayPackage.THEME_STYLE,
                ThemeOverlayPackage.COLOR_SOURCE,
                ThemeOverlayPackage.SYSTEM_PALETTE
            )

            for (key in keysToRemove) {
                jsonObject.remove(key)
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                jsonObject.remove(ThemeOverlayPackage.ACCENT_COLOR)
            }

            jsonObject.putOpt(ThemeOverlayPackage.COLOR_BOTH, "0")
            jsonObject.putOpt(ThemeOverlayPackage.COLOR_SOURCE, "home_wallpaper")
            jsonObject.putOpt(
                ThemeOverlayPackage.APPLIED_TIMESTAMP,
                System.currentTimeMillis()
            )

            return jsonObject
        }

    companion object {
        private val TAG: String = ShizukuConnection::class.java.simpleName
    }
}
