package com.drdisagree.colorblendr.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.drdisagree.colorblendr.R;

public class HeaderTextView extends androidx.appcompat.widget.AppCompatTextView {

    public HeaderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public HeaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isInEditMode();
        init(attrs);
    }

    public HeaderTextView(Context context) {
        super(context);
        isInEditMode();
        init(null);
    }

    @SuppressLint("CustomViewStyleable")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HeaderTextView);
            String fontName = a.getString(R.styleable.HeaderTextView_fontName);
            if (fontName != null) {
                Typeface myTypeface =
                        Typeface.createFromAsset(getContext().getAssets(), "fonts/" + fontName);
                setTypeface(myTypeface);
            }
            a.recycle();
        }
    }
}
