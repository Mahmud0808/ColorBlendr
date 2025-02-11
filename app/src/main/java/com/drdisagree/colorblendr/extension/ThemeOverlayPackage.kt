package com.drdisagree.colorblendr.extension

import android.graphics.Color
import android.os.Build
import android.util.Log
import com.drdisagree.colorblendr.data.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.data.common.Const.MONET_STYLE_ORIGINAL_NAME
import com.drdisagree.colorblendr.data.config.Prefs.getInt
import com.drdisagree.colorblendr.data.config.Prefs.getString
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
                        getString(
                            MONET_STYLE_ORIGINAL_NAME,
                            "TONAL_SPOT"
                        )
                    )
                    putOpt(
                        SYSTEM_PALETTE,
                        intToHexColorNoHash(
                            getInt(
                                MONET_SEED_COLOR,
                                Color.BLUE
                            )
                        )
                    )
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                        putOpt(
                            ACCENT_COLOR,
                            intToHexColorNoHash(
                                getInt(
                                    MONET_SEED_COLOR,
                                    Color.BLUE
                                )
                            )
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
