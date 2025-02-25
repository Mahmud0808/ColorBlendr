package com.drdisagree.colorblendr.extension

import android.graphics.Color
import android.os.Build
import android.util.Log
import com.drdisagree.colorblendr.data.common.Utilities.getOriginalStyleName
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.utils.ColorUtil.intToHexColorNoHash
import org.json.JSONObject

object ThemeOverlayPackage {

    private val TAG: String = ThemeOverlayPackage::class.java.simpleName
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
}
