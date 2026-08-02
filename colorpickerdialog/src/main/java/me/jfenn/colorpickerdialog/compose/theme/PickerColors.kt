package me.jfenn.colorpickerdialog.compose.theme

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import me.jfenn.colorpickerdialog.R

// Theme attr fallback chains + color helpers for picker components.
internal object PickerColors {

    @ColorInt
    fun fromAttr(context: Context, @AttrRes attr: Int, @ColorInt defaultColor: Int): Int {
        return try {
            val out = TypedValue()
            if (!context.theme.resolveAttribute(attr, out, true)) return defaultColor

            if (out.resourceId == 0) {
                if (out.data != 0) out.data else defaultColor
            } else {
                ContextCompat.getColor(context, out.resourceId)
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    @ColorInt
    fun fromAttrRes(context: Context, @AttrRes attr: Int, @ColorRes defaultColorRes: Int): Int {
        return fromAttr(context, attr, ContextCompat.getColor(context, defaultColorRes))
    }

    // Alpha bar + swatch outline: neutralColor attr -> textColorPrimary -> #000000.
    @ColorInt
    fun neutral(context: Context): Int = fromAttr(
        context,
        R.attr.neutralColor,
        fromAttrRes(context, android.R.attr.textColorPrimary, R.color.colorPickerDialog_neutral)
    )

    @ColorInt
    fun red(context: Context): Int =
        fromAttrRes(context, R.attr.redColor, R.color.colorPickerDialog_red)

    @ColorInt
    fun green(context: Context): Int =
        fromAttrRes(context, R.attr.greenColor, R.color.colorPickerDialog_green)

    @ColorInt
    fun blue(context: Context): Int =
        fromAttrRes(context, R.attr.blueColor, R.color.colorPickerDialog_blue)

    @ColorInt
    fun textColorPrimary(context: Context): Int =
        fromAttr(context, android.R.attr.textColorPrimary, Color.BLACK)

    @ColorInt
    fun textColorSecondary(context: Context): Int =
        fromAttr(context, android.R.attr.textColorSecondary, Color.DKGRAY)

    @ColorInt
    fun colorPrimary(context: Context): Int =
        fromAttr(context, androidx.appcompat.R.attr.colorPrimary, Color.BLACK)

    @ColorInt
    fun colorSurface(context: Context): Int =
        fromAttr(context, com.google.android.material.R.attr.colorSurface, Color.WHITE)

    fun getColorDarkness(@ColorInt color: Int): Double {
        if (color == Color.BLACK) return 1.0
        if (color == Color.WHITE || color == Color.TRANSPARENT) return 0.0

        return 1 - (0.259 * Color.red(color) +
                0.667 * Color.green(color) +
                0.074 * Color.blue(color)) / 255
    }

    fun isColorDark(@ColorInt color: Int): Boolean = getColorDarkness(color) > 0.4

    @ColorInt
    fun withBackground(@ColorInt color: Int, @ColorInt background: Int): Int {
        val alpha = Color.alpha(color) / 255f
        return Color.rgb(
            (Color.red(color) * alpha + Color.red(background) * (1 - alpha)).toInt(),
            (Color.green(color) * alpha + Color.green(background) * (1 - alpha)).toInt(),
            (Color.blue(color) * alpha + Color.blue(background) * (1 - alpha)).toInt()
        )
    }

    // 13 hues in 30-degree steps (0..360, first == last).
    fun colorWheelArr(saturation: Float, brightness: Float): IntArray =
        IntArray(13) { i ->
            Color.HSVToColor(floatArrayOf(i * 30f, saturation, brightness))
        }
}
