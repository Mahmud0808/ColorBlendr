package com.drdisagree.colorblendr.utils.colors

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.google.android.material.color.DynamicColors
import com.google.android.material.R as MaterialR

object ColorUtil {

    @ColorInt
    fun getAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(MaterialR.attr.colorPrimaryFixed, typedValue, true)
        return typedValue.data
    }

    fun intToHexColor(colorInt: Int): String {
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    fun intToArgbHexColor(colorInt: Int): String {
        return if (Color.alpha(colorInt) == 0xFF) intToHexColor(colorInt) else String.format("#%08X", colorInt)
    }

    fun intToHexColorNoHash(colorInt: Int): String {
        return String.format("%06X", (0xFFFFFF and colorInt))
    }

    fun calculateTextColor(@ColorInt color: Int): Int {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

        return if (darkness < 0.5) Color.BLACK else Color.WHITE
    }

    val monetAccentColors: ArrayList<Int>
        get() {
            val dynamicColorContext = DynamicColors.wrapContextIfAvailable(
                appContext,
                MaterialR.style.ThemeOverlay_Material3_DynamicColors_DayNight
            )
            val attrsToResolve = intArrayOf(
                MaterialR.attr.colorPrimaryFixed,
                MaterialR.attr.colorSecondary,
                MaterialR.attr.colorTertiary
            )
            @StyleableRes var index = 0
            val materialAccentColors = ArrayList<Int>()

            dynamicColorContext.obtainStyledAttributes(attrsToResolve).apply {
                materialAccentColors.add(getColor(index++, 0))
                materialAccentColors.add(getColor(index++, 0))
                materialAccentColors.add(getColor(index, 0))
                recycle()
            }

            return materialAccentColors
        }
}
