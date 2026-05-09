package com.drdisagree.colorblendr.utils.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.utils.app.MiscUtil.toPx

class RoundedBackgroundSpan(
    private val backgroundColor: Int,
    private val textColor: Int,
    private val paddingH: Int = appContext.toPx(6),
    private val paddingV: Int = appContext.toPx(2),
    private val radius: Int = appContext.toPx(8),
    private val textScale: Float = 0.75f,
    private val yOffsetPx: Int = 0
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val scaledPaint = Paint(paint)
        scaledPaint.textSize = paint.textSize * textScale

        val width = scaledPaint.measureText(text, start, end)

        fm?.let {
            val original = paint.fontMetricsInt

            val scaledFm = scaledPaint.fontMetricsInt
            val badgeHeight = (scaledFm.descent - scaledFm.ascent) + paddingV * 2
            val textCenter = (original.ascent + original.descent) / 2

            val newAscent = textCenter - badgeHeight / 2
            val newDescent = textCenter + badgeHeight / 2

            // Expand the line metrics if the badge is taller than the current line
            if (newAscent < it.ascent) it.ascent = newAscent
            if (newDescent > it.descent) it.descent = newDescent

            if (newAscent < it.top) it.top = newAscent
            if (newDescent > it.bottom) it.bottom = newDescent
        }

        return (width + paddingH * 2).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val scaledPaint = Paint(paint)
        scaledPaint.textSize = paint.textSize * textScale

        val width = scaledPaint.measureText(text, start, end)
        val originalFm = paint.fontMetrics
        val scaledFm = scaledPaint.fontMetrics

        // Calculate the center of the original text relative to the baseline
        val originalCenter = (originalFm.ascent + originalFm.descent) / 2
        val centerY = y + originalCenter - yOffsetPx

        // Calculate badge height based on scaled text
        val badgeHeight = (scaledFm.descent - scaledFm.ascent) + paddingV * 2

        val rect = RectF(
            x,
            centerY - badgeHeight / 2,
            x + width + paddingH * 2,
            centerY + badgeHeight / 2
        )

        scaledPaint.color = backgroundColor
        canvas.drawRoundRect(rect, radius.toFloat(), radius.toFloat(), scaledPaint)

        scaledPaint.color = textColor
        // Draw scaled text centered vertically within the badge
        val textY = centerY - (scaledFm.ascent + scaledFm.descent) / 2
        canvas.drawText(text, start, end, x + paddingH, textY, scaledPaint)
    }
}
