package com.drdisagree.colorblendr.ui.views

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat

class ColorPreview : View {

    private var squarePaint: Paint? = null
    private var secondQuarterCirclePaint: Paint? = null
    private var firstQuarterCirclePaint: Paint? = null
    private var halfCirclePaint: Paint? = null
    private var squareRect: RectF? = null
    private var circleRect: RectF? = null
    private var padding: Float = 0f

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
        val isDarkMode: Boolean =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

        padding = 10 * resources.displayMetrics.density

        squareRect = RectF()
        circleRect = RectF()

        squarePaint = Paint()
        squarePaint!!.setColor(
            ContextCompat.getColor(
                context,
                if (!isDarkMode) com.google.android.material.R.color.material_dynamic_neutral99 else com.google.android.material.R.color.material_dynamic_neutral10
            )
        )
        squarePaint!!.style = Paint.Style.FILL

        halfCirclePaint = Paint()
        halfCirclePaint!!.setColor(
            ContextCompat.getColor(
                context,
                com.google.android.material.R.color.material_dynamic_primary90
            )
        )
        halfCirclePaint!!.style = Paint.Style.FILL

        firstQuarterCirclePaint = Paint()
        firstQuarterCirclePaint!!.setColor(
            ContextCompat.getColor(
                context,
                com.google.android.material.R.color.material_dynamic_secondary90
            )
        )
        firstQuarterCirclePaint!!.style = Paint.Style.FILL

        secondQuarterCirclePaint = Paint()
        secondQuarterCirclePaint!!.setColor(
            ContextCompat.getColor(
                context,
                com.google.android.material.R.color.material_dynamic_tertiary90
            )
        )
        secondQuarterCirclePaint!!.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cornerRadius: Float = 12 * resources.displayMetrics.density
        squareRect!!.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(squareRect!!, cornerRadius, cornerRadius, squarePaint!!)

        val margin: Float = 0 * resources.displayMetrics.density

        circleRect!!.set(padding, padding, width - padding, height - padding - margin)
        canvas.drawArc(circleRect!!, 180f, 180f, true, halfCirclePaint!!)

        circleRect!!.set(padding, padding + margin, width - padding - margin, height - padding)
        canvas.drawArc(circleRect!!, 90f, 90f, true, firstQuarterCirclePaint!!)

        circleRect!!.set(padding + margin, padding + margin, width - padding, height - padding)
        canvas.drawArc(circleRect!!, 0f, 90f, true, secondQuarterCirclePaint!!)
    }

    fun setSquareColor(@ColorInt color: Int) {
        squarePaint!!.setColor(color)
    }

    fun setFirstQuarterCircleColor(@ColorInt color: Int) {
        firstQuarterCirclePaint!!.setColor(color)
    }

    fun setSecondQuarterCircleColor(@ColorInt color: Int) {
        secondQuarterCirclePaint!!.setColor(color)
    }

    fun setHalfCircleColor(@ColorInt color: Int) {
        halfCirclePaint!!.setColor(color)
    }

    fun invalidateColors() {
        invalidate()
    }

    fun setPadding(padding: Float) {
        this.padding = padding * resources.displayMetrics.density
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        this.setAlpha(if (enabled) 1.0f else 0.6f)
    }
}
