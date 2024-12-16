package com.drdisagree.colorblendr.utils

import android.os.Build
import com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.extension.ThemeOverlayPackage
import com.topjohnwu.superuser.Shell
import org.json.JSONException
import org.json.JSONObject

object SystemPalette {

    fun applyFabricatedColors(jsonString: String) {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'"
        ).exec()
    }

    fun removeFabricatedColors() {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$originalSettings'"
        ).exec()
    }

    val currentSettings: String
        get() {
            val currentSettings = Shell.cmd(
                "settings get secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES"
            ).exec().out[0]

            return if (currentSettings == "null" || currentSettings.isEmpty()) {
                JSONObject().toString()
            } else {
                currentSettings
            }
        }

    @get:Throws(JSONException::class)
    val originalSettings: JSONObject
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