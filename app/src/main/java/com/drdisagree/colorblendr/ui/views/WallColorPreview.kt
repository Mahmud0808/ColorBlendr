package com.drdisagree.colorblendr.ui.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getCurrentMonetStyle
import com.drdisagree.colorblendr.utils.ColorUtil.calculateTextColor
import com.drdisagree.colorblendr.utils.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.SystemUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallColorPreview : View {

    private var context: Context? = null
    private var isDarkMode = false
    private var tickPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    private var squarePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var centerCirclePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var centerClearCirclePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var halfCirclePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.BUTT
    }
    private var firstQuarterCirclePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.BUTT
    }
    private var secondQuarterCirclePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.BUTT
    }
    private var squareRect: RectF = RectF()
    private var circleRect: RectF = RectF()
    private var tickPath: Path = Path()
    private var circleRadius = 10 * resources.displayMetrics.density
    private var clearCircleRadius = circleRadius + 0 * resources.displayMetrics.density
    private var isSelected = false
    private var colorPalette: ArrayList<ArrayList<Int>>? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    @ColorInt
    private var halfCircleColor = Color.DKGRAY

    @ColorInt
    private var firstQuarterCircleColor = Color.DKGRAY

    @ColorInt
    private var secondQuarterCircleColor = Color.DKGRAY

    @ColorInt
    private var squareColor = Color.DKGRAY

    @ColorInt
    private var centerCircleColor = Color.DKGRAY

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        this.context = context

        isDarkMode =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

        circleRadius = 10 * resources.displayMetrics.density
        clearCircleRadius = circleRadius + 0 * resources.displayMetrics.density

        squarePaint.color = ContextCompat.getColor(
            context,
            if (!isDarkMode) com.google.android.material.R.color.material_dynamic_neutral99
            else com.google.android.material.R.color.material_dynamic_neutral10
        )
        halfCirclePaint.color = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.material_dynamic_primary90
        )
        firstQuarterCirclePaint.color = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.material_dynamic_secondary90
        )
        secondQuarterCirclePaint.color = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.material_dynamic_tertiary90
        )
        centerCirclePaint.color = ContextCompat.getColor(
            context,
            com.google.android.material.R.color.material_dynamic_primary70
        )
        centerClearCirclePaint.color = ContextCompat.getColor(
            context,
            if (!isDarkMode) com.google.android.material.R.color.material_dynamic_neutral99
            else com.google.android.material.R.color.material_dynamic_neutral10
        )
        tickPaint.color = ContextCompat.getColor(
            context,
            if (!isDarkMode) com.google.android.material.R.color.material_dynamic_neutral99
            else com.google.android.material.R.color.material_dynamic_neutral10
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height

        val cornerRadius = 12 * resources.displayMetrics.density
        squareRect[0f, 0f, width.toFloat()] = height.toFloat()
        canvas.drawRoundRect(squareRect, cornerRadius, cornerRadius, squarePaint)

        val padding = 6 * resources.displayMetrics.density
        val margin = 0 * resources.displayMetrics.density

        circleRect[padding, padding, width - padding] = height - padding - margin
        canvas.drawArc(circleRect, 180f, 180f, true, halfCirclePaint)

        circleRect[padding, padding + margin, width - padding - margin] = height - padding
        canvas.drawArc(circleRect, 90f, 90f, true, firstQuarterCirclePaint)

        circleRect[padding + margin, padding + margin, width - padding] = height - padding
        canvas.drawArc(circleRect, 0f, 90f, true, secondQuarterCirclePaint)

        circleRect[width / 2f - clearCircleRadius, height / 2f - clearCircleRadius, width / 2f + clearCircleRadius] =
            height / 2f + clearCircleRadius
        canvas.drawArc(circleRect, 0f, 360f, true, centerClearCirclePaint)

        circleRect[width / 2f - circleRadius, height / 2f - circleRadius, width / 2f + circleRadius] =
            height / 2f + circleRadius
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, centerCirclePaint)

        if (isSelected) {
            tickPath.moveTo(width / 2f - circleRadius / 2, height / 2f)
            tickPath.lineTo(width / 2f - circleRadius / 6, height / 2f + circleRadius / 3)
            tickPath.lineTo(width / 2f + circleRadius / 2, height / 2f - circleRadius / 3)
            canvas.drawPath(tickPath, tickPaint)
        }
    }

    private fun setSquareColor(@ColorInt color: Int) {
        squareColor = color
        squarePaint.color = color
        centerClearCirclePaint.color = color
    }

    private fun setFirstQuarterCircleColor(@ColorInt color: Int) {
        firstQuarterCircleColor = color
        firstQuarterCirclePaint.color = color
    }

    private fun setSecondQuarterCircleColor(@ColorInt color: Int) {
        secondQuarterCircleColor = color
        secondQuarterCirclePaint.color = color
    }

    private fun setHalfCircleColor(@ColorInt color: Int) {
        halfCircleColor = color
        halfCirclePaint.color = color
    }

    private fun setCenterCircleColor(@ColorInt color: Int) {
        centerCircleColor = color
        centerCirclePaint.color = color
        @ColorInt val textColor = calculateTextColor(color)
        tickPaint.color = if (colorPalette != null) {
            colorPalette!![4][if (textColor == Color.WHITE) 2 else 11]
        } else {
            textColor
        }
    }

    private fun invalidateColors() {
        invalidate()
    }

    fun setMainColor(@ColorInt color: Int) {
        coroutineScope.launch {
            try {
                colorPalette = withContext(Dispatchers.IO) {
                    generateModifiedColors(
                        getCurrentMonetStyle(),
                        color,
                        getInt(MONET_ACCENT_SATURATION, 100),
                        getInt(MONET_BACKGROUND_SATURATION, 100),
                        getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                        getBoolean(MONET_PITCH_BLACK_THEME, false),
                        getBoolean(MONET_ACCURATE_SHADES, true),
                        false,
                        SystemUtil.isDarkMode,
                        false
                    )
                }

                withContext(Dispatchers.Main) {
                    setHalfCircleColor(colorPalette!![0][4])
                    setFirstQuarterCircleColor(colorPalette!![2][5])
                    setSecondQuarterCircleColor(colorPalette!![1][6])
                    setSquareColor(colorPalette!![4][if (!isDarkMode) 3 else 9])
                    setCenterCircleColor(color)
                    invalidateColors()
                }
            } catch (ignored: Exception) {
            }
        }
    }

    override fun isSelected(): Boolean {
        return isSelected
    }

    override fun setSelected(selected: Boolean) {
        isSelected = selected
        invalidateColors()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.halfCircleColor = halfCircleColor
        ss.firstQuarterCircleColor = firstQuarterCircleColor
        ss.secondQuarterCircleColor = secondQuarterCircleColor
        ss.squareColor = squareColor
        ss.centerCircleColor = centerCircleColor

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        setHalfCircleColor(state.halfCircleColor)
        setFirstQuarterCircleColor(state.firstQuarterCircleColor)
        setSecondQuarterCircleColor(state.secondQuarterCircleColor)
        setSquareColor(state.squareColor)
        setCenterCircleColor(state.centerCircleColor)
    }

    private class SavedState : BaseSavedState {
        @ColorInt
        var halfCircleColor: Int = 0

        @ColorInt
        var firstQuarterCircleColor: Int = 0

        @ColorInt
        var secondQuarterCircleColor: Int = 0

        @ColorInt
        var squareColor: Int = 0

        @ColorInt
        var centerCircleColor: Int = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            halfCircleColor = `in`.readInt()
            firstQuarterCircleColor = `in`.readInt()
            secondQuarterCircleColor = `in`.readInt()
            squareColor = `in`.readInt()
            centerCircleColor = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(halfCircleColor)
            dest.writeInt(firstQuarterCircleColor)
            dest.writeInt(secondQuarterCircleColor)
            dest.writeInt(squareColor)
            dest.writeInt(centerCircleColor)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        this.alpha = if (enabled) 1.0f else 0.6f
    }
}
