package me.jfenn.colorpickerdialog.views.color;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class HorizontalSmoothColorView extends SmoothColorView {

    public HorizontalSmoothColorView(Context context) {
        super(context);
    }

    public HorizontalSmoothColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalSmoothColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public HorizontalSmoothColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = (int) (getMeasuredWidth() * 0.5625);
        setMeasuredDimension(getMeasuredWidth(), height);
    }
}
