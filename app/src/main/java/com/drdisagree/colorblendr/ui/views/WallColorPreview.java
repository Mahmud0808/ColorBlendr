package com.drdisagree.colorblendr.ui.views;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

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

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.SystemUtil;

import java.util.ArrayList;

public class WallColorPreview extends View {

    private Context context;
    private boolean isDarkMode;
    private Paint squarePaint, centerCirclePaint, centerClearCirclePaint, secondQuarterCirclePaint, firstQuarterCirclePaint, halfCirclePaint;
    private RectF squareRect, circleRect;
    private float clearCircleRadius, circleRadius;

    public WallColorPreview(Context context) {
        super(context);
        init(context);
    }

    public WallColorPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WallColorPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        isDarkMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;

        circleRadius = 8 * getResources().getDisplayMetrics().density;
        clearCircleRadius = circleRadius + 2 * getResources().getDisplayMetrics().density;

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
        halfCirclePaint.setStrokeCap(Paint.Cap.BUTT);

        firstQuarterCirclePaint = new Paint();
        firstQuarterCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_secondary90));
        firstQuarterCirclePaint.setStyle(Paint.Style.FILL);
        firstQuarterCirclePaint.setStrokeCap(Paint.Cap.BUTT);

        secondQuarterCirclePaint = new Paint();
        secondQuarterCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_tertiary90));
        secondQuarterCirclePaint.setStyle(Paint.Style.FILL);
        secondQuarterCirclePaint.setStrokeCap(Paint.Cap.BUTT);

        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(ContextCompat.getColor(context, com.google.android.material.R.color.material_dynamic_primary70));
        centerCirclePaint.setStyle(Paint.Style.FILL);

        centerClearCirclePaint = new Paint();
        centerClearCirclePaint.setColor(ContextCompat.getColor(context,
                !isDarkMode ?
                        com.google.android.material.R.color.material_dynamic_neutral99 :
                        com.google.android.material.R.color.material_dynamic_neutral10
        ));
        centerClearCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float cornerRadius = 12 * getResources().getDisplayMetrics().density;
        squareRect.set(0, 0, width, height);
        canvas.drawRoundRect(squareRect, cornerRadius, cornerRadius, squarePaint);

        float padding = 6 * getResources().getDisplayMetrics().density;
        float margin = 2 * getResources().getDisplayMetrics().density;

        circleRect.set(padding, padding, width - padding, height - padding - margin);
        canvas.drawArc(circleRect, 180, 180, true, halfCirclePaint);

        circleRect.set(padding, padding + margin, width - padding - margin, height - padding);
        canvas.drawArc(circleRect, 90, 90, true, firstQuarterCirclePaint);

        circleRect.set(padding + margin, padding + margin, width - padding, height - padding);
        canvas.drawArc(circleRect, 0, 90, true, secondQuarterCirclePaint);

        circleRect.set(width / 2f - clearCircleRadius, height / 2f - clearCircleRadius, width / 2f + clearCircleRadius, height / 2f + clearCircleRadius);
        canvas.drawArc(circleRect, 0, 360, true, centerClearCirclePaint);

        circleRect.set(width / 2f - circleRadius, height / 2f - circleRadius, width / 2f + circleRadius, height / 2f + circleRadius);
        canvas.drawCircle(width / 2f, height / 2f, circleRadius, centerCirclePaint);
    }

    private void setSquareColor(@ColorInt int color) {
        squarePaint.setColor(color);
        centerClearCirclePaint.setColor(color);
    }

    private void setFirstQuarterCircleColor(@ColorInt int color) {
        firstQuarterCirclePaint.setColor(color);
    }

    private void setSecondQuarterCircleColor(@ColorInt int color) {
        secondQuarterCirclePaint.setColor(color);
    }

    private void setHalfCircleColor(@ColorInt int color) {
        halfCirclePaint.setColor(color);
    }

    private void setCenterCircleColor(@ColorInt int color) {
        centerCirclePaint.setColor(color);
    }

    private void invalidateColors() {
        invalidate();
    }

    public void setMainColor(@ColorInt int color) {
        new Thread(() -> {
            try {
                ArrayList<ArrayList<Integer>> colorPalette = ColorUtil.generateModifiedColors(
                        ColorSchemeUtil.stringToEnumMonetStyle(
                                context,
                                RPrefs.getString(MONET_STYLE, context.getString(R.string.monet_tonalspot))
                        ),
                        color,
                        RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                        RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                        RPrefs.getBoolean(MONET_ACCURATE_SHADES, true),
                        false,
                        SystemUtil.isDarkMode()
                );

                setHalfCircleColor(colorPalette.get(0).get(!isDarkMode ? 4 : 5));
                setFirstQuarterCircleColor(colorPalette.get(2).get(!isDarkMode ? 4 : 5));
                setSecondQuarterCircleColor(colorPalette.get(1).get(!isDarkMode ? 4 : 5));
                setSquareColor(colorPalette.get(4).get(!isDarkMode ? 2 : 9));
                setCenterCircleColor(color);
                invalidateColors();
            } catch (Exception ignored) {
            }
        }).start();
    }
}
