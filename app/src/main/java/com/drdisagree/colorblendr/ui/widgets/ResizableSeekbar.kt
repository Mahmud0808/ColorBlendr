package com.drdisagree.colorblendr.ui.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class ResizableSeekbar extends androidx.appcompat.widget.AppCompatSeekBar {

    public ResizableSeekbar(Context context) {
        super(context);
    }

    public ResizableSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResizableSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected synchronized void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Drawable trackDrawable = getProgressDrawable().getCurrent();
        trackDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}
