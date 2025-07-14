package com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.views.color;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import me.jfenn.androidutils.DimenUtilsKt;

public class VerticalSmoothColorView extends SmoothColorView {

    public VerticalSmoothColorView(Context context) {
        super(context);
    }

    public VerticalSmoothColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSmoothColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public VerticalSmoothColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredHeight();
        setMeasuredDimension(Math.min(DimenUtilsKt.dpToPx(200), size), size);
    }
}
