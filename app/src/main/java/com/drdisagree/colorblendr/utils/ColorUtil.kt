package com.drdisagree.colorblendr.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.MONET
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.generateColorPalette
import com.drdisagree.colorblendr.utils.cam.Cam
import com.google.gson.reflect.TypeToken
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min
import kotlin.math.pow

object ColorUtil {

    @ColorInt
    fun getColorFromAttribute(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    fun generateModifiedColors(
        style: MONET,
        accentSaturation: Int,
        backgroundSaturation: Int,
        backgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean,
        modifyPitchBlack: Boolean = true,
        isDark: Boolean = SystemUtil.isDarkMode
    ): ArrayList<ArrayList<Int>> {
        val wallpaperColorList: ArrayList<Int>? =
            RPrefs.getString(Const.WALLPAPER_COLOR_LIST, null)?.let {
                Const.GSON.fromJson(
                    it,
                    object : TypeToken<ArrayList<Int>>() {}.type
                )
            }

        if (wallpaperColorList == null) {
            throw Exception("No wallpaper color list found")
        }

        return generateModifiedColors(
            style,
            RPrefs.getInt(
                Const.MONET_SEED_COLOR,
                wallpaperColorList[0]
            ),
            accentSaturation,
            backgroundSaturation,
            backgroundLightness,
            pitchBlackTheme,
            accurateShades,
            modifyPitchBlack,
            isDark,
            true
        )
    }

    fun generateModifiedColors(
        style: MONET,
        @ColorInt seedColor: Int,
        accentSaturation: Int,
        backgroundSaturation: Int,
        backgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean,
        modifyPitchBlack: Boolean,
        isDark: Boolean,
        overrideColors: Boolean
    ): ArrayList<ArrayList<Int>> {
        val palette: ArrayList<ArrayList<Int>> = generateColorPalette(
            style,
            seedColor,
            isDark
        )

        // Modify colors
        for (i in palette.indices) {
            val modifiedShades = ColorModifiers.modifyColors(
                ArrayList(palette[i].subList(1, palette[i].size)),
                AtomicInteger(i),
                style,
                accentSaturation,
                backgroundSaturation,
                backgroundLightness,
                pitchBlackTheme,
                accurateShades,
                modifyPitchBlack,
                overrideColors
            )
            for (j in 1 until palette[i].size) {
                palette[i][j] = modifiedShades[j - 1]
            }
        }

        return palette
    }

    @ColorInt
    fun getAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary,
            typedValue,
            true
        )
        return typedValue.data
    }

    fun modifySaturation(color: Int, saturation: Int): Int {
        val saturationFloat = (saturation - 100) / 100f

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)

        if (saturationFloat > 0) {
            hsl[1] += ((1 - hsl[1]) * saturationFloat)
        } else if (saturationFloat < 0) {
            hsl[1] += (hsl[1] * saturationFloat)
        }

        return ColorUtils.HSLToColor(hsl)
    }

    fun modifyLightness(color: Int, lightness: Int, idx: Int): Int {
        var lightnessFloat = (lightness - 100) / 1000f
        val shade = systemTintList[idx]

        when (idx) {
            0, 12 -> {
                lightnessFloat = 0f
            }

            1 -> {
                lightnessFloat /= 10f
            }

            2 -> {
                lightnessFloat /= 2f
            }
        }

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)

        hsl[2] = shade + lightnessFloat

        return ColorUtils.HSLToColor(hsl)
    }

    fun modifyBrightness(color: Int, brightnessPercentage: Int): Int {
        // Ensure brightnessPercentage is within -100 to 100
        val clampedPercentage = brightnessPercentage.coerceIn(-100, 100)

        // Convert brightness percentage to a factor
        val factor = 1.0f + (clampedPercentage / 100f)

        // Extract RGB components
        val r = (color shr 16 and 0xFF) / 255f
        val g = (color shr 8 and 0xFF) / 255f
        val b = (color and 0xFF) / 255f

        // Calculate current brightness
        val currentBrightness = 0.2126f * r + 0.7152f * g + 0.0722f * b

        // Determine the adjustment factor to achieve the desired brightness
        val adjustedFactor = if (currentBrightness == 0f) 0f else factor

        // Adjust RGB components by the factor
        val newR = (r * adjustedFactor).coerceIn(0f, 1f)
        val newG = (g * adjustedFactor).coerceIn(0f, 1f)
        val newB = (b * adjustedFactor).coerceIn(0f, 1f)

        // Convert back to color integer
        val newRInt = (newR * 255).toInt()
        val newGInt = (newG * 255).toInt()
        val newBInt = (newB * 255).toInt()

        // Return the adjusted color, preserving the alpha channel
        return (color and 0xFF000000.toInt()) or
                (newRInt shl 16) or
                (newGInt shl 8) or
                newBInt
    }

    fun getHue(color: Int): Float {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)

        return hsl[0]
    }

    private val systemTintList: FloatArray
        get() = floatArrayOf(
            1.0f,
            0.99f,
            0.95f,
            0.9f,
            0.8f,
            0.7f,
            0.6f,
            0.496f,
            0.4f,
            0.3f,
            0.2f,
            0.1f,
            0.0f
        )

    val colorNames: Array<Array<String>>
        get() {
            val accentTypes = arrayOf(
                "system_accent1",
                "system_accent2",
                "system_accent3",
                "system_neutral1",
                "system_neutral2"
            )
            val values = arrayOf(
                "0",
                "10",
                "50",
                "100",
                "200",
                "300",
                "400",
                "500",
                "600",
                "700",
                "800",
                "900",
                "1000"
            )

            val colorNames = Array(accentTypes.size) {
                Array(values.size) { "" }
            }

            for (i in accentTypes.indices) {
                for (j in values.indices) {
                    colorNames[i][j] = accentTypes[i] + "_" + values[j]
                }
            }

            return colorNames
        }

    fun getColorNamesM3(isDynamic: Boolean, prefixG: Boolean): Array<Array<String>> {
        val prefix = "m3_ref_palette_"
        val dynamic = "dynamic_"

        val accentTypes = arrayOf("primary", "secondary", "tertiary", "neutral", "neutral_variant")
        val values =
            arrayOf("100", "99", "95", "90", "80", "70", "60", "50", "40", "30", "20", "10", "0")

        val colorNames = Array(accentTypes.size) { Array(values.size) { "" } }

        for (i in accentTypes.indices) {
            for (j in values.indices) {
                colorNames[i][j] =
                    (if (prefixG) "g" else "") + prefix + (if (isDynamic) dynamic else "") + accentTypes[i] + values[j]
            }
        }

        return colorNames
    }

    fun intToHexColor(colorInt: Int): String {
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }

    fun intToHexColorNoHash(colorInt: Int): String {
        return String.format("%06X", (0xFFFFFF and colorInt))
    }

    fun getSystemColors(context: Context): Array<IntArray> {
        return arrayOf(
            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_primary0,
                    context.theme
                )
            ),

            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_secondary0,
                    context.theme
                )
            ),

            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_tertiary0,
                    context.theme
                )
            ),

            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral0,
                    context.theme
                )
            ),

            intArrayOf(
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant100,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant99,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant95,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant90,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant80,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant70,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant60,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant50,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant40,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant30,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant20,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant10,
                    context.theme
                ),
                context.resources.getColor(
                    com.google.android.material.R.color.material_dynamic_neutral_variant0,
                    context.theme
                )
            )
        )
    }

    fun calculateTextColor(@ColorInt color: Int): Int {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

        return if (darkness < 0.5) Color.BLACK else Color.WHITE
    }

    /**
     * Convert a color appearance model representation to an ARGB color.
     *
     *
     * Note: the returned color may have a lower chroma than requested. Whether a chroma is
     * available depends on luminance. For example, there's no such thing as a high chroma light
     * red, due to the limitations of our eyes and/or physics. If the requested chroma is
     * unavailable, the highest possible chroma at the requested luminance is returned.
     *
     * @param hue    hue, in degrees, in CAM coordinates
     * @param chroma chroma in CAM coordinates.
     * @param lstar  perceptual luminance, L* in L*a*b*
     */
    @JvmStatic
    @ColorInt
    fun CAMToColor(hue: Float, chroma: Float, lstar: Float): Int {
        return Cam.getInt(hue, chroma, lstar)
    }

    private const val XYZ_WHITE_REFERENCE_X = 95.047
    private const val XYZ_WHITE_REFERENCE_Y = 100.0
    private const val XYZ_WHITE_REFERENCE_Z = 108.883

    /**
     * Converts a color from CIE XYZ to its RGB representation.
     *
     *
     * This method expects the XYZ representation to use the D65 illuminant and the CIE
     * 2° Standard Observer (1931).
     *
     * @param x X component value [0...95.047)
     * @param y Y component value [0...100)
     * @param z Z component value [0...108.883)
     * @return int containing the RGB representation
     */
    @JvmStatic
    @ColorInt
    fun XYZToColor(
        @FloatRange(from = 0.0, to = XYZ_WHITE_REFERENCE_X) x: Double,
        @FloatRange(from = 0.0, to = XYZ_WHITE_REFERENCE_Y) y: Double,
        @FloatRange(from = 0.0, to = XYZ_WHITE_REFERENCE_Z) z: Double
    ): Int {
        var r = (x * 3.2406 + y * -1.5372 + z * -0.4986) / 100
        var g = (x * -0.9689 + y * 1.8758 + z * 0.0415) / 100
        var b = (x * 0.0557 + y * -0.2040 + z * 1.0570) / 100

        r = if (r > 0.0031308) 1.055 * r.pow(1 / 2.4) - 0.055 else 12.92 * r
        g = if (g > 0.0031308) 1.055 * g.pow(1 / 2.4) - 0.055 else 12.92 * g
        b = if (b > 0.0031308) 1.055 * b.pow(1 / 2.4) - 0.055 else 12.92 * b

        return Color.rgb(
            constrain(Math.round(r * 255).toInt(), 0, 255),
            constrain(Math.round(g * 255).toInt(), 0, 255),
            constrain(Math.round(b * 255).toInt(), 0, 255)
        )
    }

    private fun constrain(amount: Float, low: Float, high: Float): Float {
        return if (amount < low) low else min(amount.toDouble(), high.toDouble()).toFloat()
    }

    @Suppress("SameParameterValue")
    private fun constrain(amount: Int, low: Int, high: Int): Int {
        return if (amount < low) low else min(amount.toDouble(), high.toDouble()).toInt()
    }

    val monetAccentColors: ArrayList<Int>
        get() {
            val colors = ArrayList<Int>()
            colors.add(
                ContextCompat.getColor(
                    appContext,
                    android.R.color.system_accent1_400
                )
            )
            colors.add(
                ContextCompat.getColor(
                    appContext,
                    android.R.color.system_accent2_400
                )
            )
            colors.add(
                ContextCompat.getColor(
                    appContext,
                    android.R.color.system_accent3_400
                )
            )
            return colors
        }
}
