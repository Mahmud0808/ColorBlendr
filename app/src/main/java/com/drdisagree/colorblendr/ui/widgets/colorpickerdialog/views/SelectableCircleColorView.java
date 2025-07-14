package com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatImageView;

import com.drdisagree.colorblendr.R;

import me.jfenn.androidutils.DimenUtilsKt;
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.utils.ColorUtils;

public class SelectableCircleColorView extends AppCompatImageView {
    private static final float MAX_SCALE = 1;
    private static final float MIN_SCALE = 0.8F;
    private final Paint alphaGridPaint = getAlphaGridPattern();
    private boolean showsAlphaGrid = false;

    public SelectableCircleColorView(Context context) {
        super(context);
        init();
    }

    public SelectableCircleColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableCircleColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleX(0);
        setScaleY(0);
        setSelected(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //height will be equal to the measured width
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    /**
     * Sets the color that should display as a circle.
     * If the color matches certain criteria, a border will be automatically added
     *
     * @param color The color that we want to apply
     */
    public void setColor(@ColorInt int color) {
        Context context = getContext();
        int neutralColor = ColorUtils.fromAttr(context, R.attr.neutralColor,
                ColorUtils.fromAttrRes(context, android.R.attr.textColorPrimary, R.color.colorPickerDialog_neutral));

        int outlineColor = (ColorUtils.isColorDark(neutralColor)
                ? (ColorUtils.isColorDark(color) ? color : neutralColor)
                : (ColorUtils.isColorDark(color) ? neutralColor : color));

        int strokeWidth = DimenUtilsKt.dpToPx(2F);
        GradientDrawable colorSwatchDrawable = new GradientDrawable();
        colorSwatchDrawable.setColor(color);
        colorSwatchDrawable.setShape(GradientDrawable.OVAL);
        colorSwatchDrawable.setStroke(strokeWidth, outlineColor);

        showsAlphaGrid = (Color.alpha(color) < 255);

        setImageDrawable(colorSwatchDrawable);
    }

    /**
     * Specify whether the color is currently "selected".
     * <p>
     * This animates the color circle's scale between 100%
     * and 80% of the size of the view depending on whether
     * it is selected or unselected, respectively.
     * <p>
     * If this method is not called, the view defaults to the
     * "selected" state.
     *
     * @param isSelected Whether the view is selected.
     */
    public void setSelected(boolean isSelected) {
        float scale = isSelected ? MAX_SCALE : MIN_SCALE;
        animate().scaleX(scale).scaleY(scale)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Generates a Paint object that simulates the alpha-grid background
     * that is associated with alpha transparency
     *
     * @return Alpha-Grid Paint
     */
    private Paint getAlphaGridPattern() {
        int squareSize = DimenUtilsKt.dpToPx(8);
        Bitmap bitmap = Bitmap.createBitmap(squareSize * 2, squareSize * 2, Bitmap.Config.ARGB_8888);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(Color.LTGRAY);

        Canvas canvas = new Canvas(bitmap);
        Rect rect = new Rect(0, 0, squareSize, squareSize);
        canvas.drawRect(rect, fill);
        rect.offset(squareSize, squareSize);
        canvas.drawRect(rect, fill);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT));
        return paint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (showsAlphaGrid) {
            int width = getMeasuredWidth();
            canvas.drawCircle(width / 2F, width / 2F, width / 2F, alphaGridPaint);
        }
        super.onDraw(canvas);
    }
}