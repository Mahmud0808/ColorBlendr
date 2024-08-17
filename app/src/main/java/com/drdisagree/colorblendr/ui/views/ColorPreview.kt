package com.drdisagree.colorblendr.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class ColorPreview extends View {

    private Paint squarePaint, secondQuarterCirclePaint, firstQuarterCirclePaint, halfCirclePaint;
    private RectF squareRect, circleRect;
    private float padding;

    public ColorPreview(Context context) {
        super(context);
        init(context);
    }

    public ColorPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ColorPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;

        padding = 10 * getResources().getDisplayMetrics().density;

        squareRect = new RectF();
        circleRect = new RectF();

        squarePaint = new Paint();
        squarePaint.setColor(ContextCompat.getColor(context,
                !isDarkMode ?
                        com.google.android.material.R.color.material_dynamic_neutral99 :
                        com.google.android.material.R.color.material_dynamic_neutral10
        ));
        squarePaint.setStyle(Paint.Style.FILL);

        halfCirclePaint = new Paint();
        halfCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_primary90));
        halfCirclePaint.setStyle(Paint.Style.FILL);

        firstQuarterCirclePaint = new Paint();
        firstQuarterCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_secondary90));
        firstQuarterCirclePaint.setStyle(Paint.Style.FILL);

        secondQuarterCirclePaint = new Paint();
        secondQuarterCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_tertiary90));
        secondQuarterCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float cornerRadius = 12 * getResources().getDisplayMetrics().density;
        squareRect.set(0, 0, width, height);
        canvas.drawRoundRect(squareRect, cornerRadius, cornerRadius, squarePaint);

        float margin = 0 * getResources().getDisplayMetrics().density;

        circleRect.set(padding, padding, width - padding, height - padding - margin);
        canvas.drawArc(circleRect, 180, 180, true, halfCirclePaint);

        circleRect.set(padding, padding + margin, width - padding - margin, height - padding);
        canvas.drawArc(circleRect, 90, 90, true, firstQuarterCirclePaint);

        circleRect.set(padding + margin, padding + margin, width - padding, height - padding);
        canvas.drawArc(circleRect, 0, 90, true, secondQuarterCirclePaint);
    }

    public void setSquareColor(@ColorInt int color) {
        squarePaint.setColor(color);
    }

    public void setFirstQuarterCircleColor(@ColorInt int color) {
        firstQuarterCirclePaint.setColor(color);
    }

    public void setSecondQuarterCircleColor(@ColorInt int color) {
        secondQuarterCirclePaint.setColor(color);
    }

    public void setHalfCircleColor(@ColorInt int color) {
        halfCirclePaint.setColor(color);
    }

    public void invalidateColors() {
        invalidate();
    }

    public void setPadding(float padding) {
        this.padding = padding * getResources().getDisplayMetrics().density;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.setAlpha(enabled ? 1.0f : 0.6f);
    }
}
