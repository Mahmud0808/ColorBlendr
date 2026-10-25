package com.drdisagree.colorblendr.extension

import android.graphics.Color
import android.os.Build
import android.util.Log
import com.drdisagree.colorblendr.data.common.Utilities.getOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.utils.app.MiscUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToHexColorNoHash
import org.json.JSONObject
import org.json.JSONTokener

object ThemeOverlayPackage {

    private val TAG: String = "ThemeOverlayPackage"
    const val THEME_STYLE: String = "android.theme.customization.theme_style"
    const val COLOR_SOURCE: String = "android.theme.customization.color_source"
    const val SYSTEM_PALETTE: String = "android.theme.customization.system_palette"
    const val ACCENT_COLOR: String = "android.theme.customization.accent_color"
    const val COLOR_BOTH: String = "android.theme.customization.color_both"
    const val APPLIED_TIMESTAMP: String = "_applied_timestamp"

    val themeCustomizationOverlayPackages: JSONObject
        get() {
            return try {
                JSONObject().apply {
                    putOpt(
                        COLOR_SOURCE,
                        "preset"
                    )
                    putOpt(
                        THEME_STYLE,
                        getOriginalStyleName()
                    )
                    putOpt(
                        SYSTEM_PALETTE,
                        intToHexColorNoHash(getSeedColorValue(Color.BLUE))
                    )
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        putOpt(
                            ACCENT_COLOR,
                            intToHexColorNoHash(getSeedColorValue(Color.BLUE))
                        )
                    }
                    putOpt(
                        APPLIED_TIMESTAMP,
                        System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "themeCustomizationOverlayPackages:", e)
                JSONObject()
            }
        }

    fun mergeIntoSettings(currentSettings: String?, source: String): String {
        val json = decodeSettings(currentSettings)
        MiscUtil.mergeJsonObjects(json, JSONObject(source.ifEmpty { "{}" }))
        return json.toString()
    }

    fun getOriginalSettings(currentSettings: String?): String {
        val json = decodeSettings(currentSettings)
        json.apply {
            val keysToRemove = arrayOf(
                THEME_STYLE,
                COLOR_SOURCE,
                SYSTEM_PALETTE
            )

            for (key in keysToRemove) {
                remove(key)
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                remove(ACCENT_COLOR)
            }

            putOpt(COLOR_BOTH, "0")
            putOpt(COLOR_SOURCE, "home_wallpaper")
            putOpt(APPLIED_TIMESTAMP, System.currentTimeMillis())
        }
        return json.toString()
    }

    private fun decodeSettings(raw: String?): JSONObject {
        val text = raw?.trim().takeUnless { it.isNullOrEmpty() || it == "null" } ?: return JSONObject()
        return runCatching {
            when (val value = JSONTokener(text).nextValue()) {
                is JSONObject -> value
                is String -> JSONObject(value)
                else -> JSONObject()
            }
        }.getOrElse {
            Log.w(TAG, "decodeSettings: discarding unparsable value: $text", it)
            JSONObject()
        }
    }

    fun String.shellQuoted(): String = "'" + replace("'", "'\\''") + "'"
}
