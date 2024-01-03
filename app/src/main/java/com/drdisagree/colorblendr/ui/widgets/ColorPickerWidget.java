package com.drdisagree.colorblendr.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.google.android.material.card.MaterialCardView;

public class ColorPickerWidget extends RelativeLayout {

    private MaterialCardView container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private View colorView;
    private @ColorInt int selectedColor = Color.WHITE;

    public ColorPickerWidget(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_colorpicker, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerWidget);
        setTitle(typedArray.getString(R.styleable.ColorPickerWidget_titleText));
        setSummary(typedArray.getString(R.styleable.ColorPickerWidget_summaryText));
        int colorResId = typedArray.getResourceId(R.styleable.ColorPickerWidget_previewColor, Integer.MIN_VALUE);
        selectedColor = typedArray.getColor(R.styleable.ColorPickerWidget_previewColor, Color.WHITE);
        typedArray.recycle();

        if (colorResId != Integer.MIN_VALUE) {
            setPreviewColor(ContextCompat.getColor(getContext(), colorResId));
        }
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSummary(int summaryResId) {
        summaryTextView.setText(summaryResId);
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);
    }

    public @ColorInt int getPreviewColor() {
        return selectedColor;
    }

    public void setPreviewColor(@ColorInt int color) {
        this.selectedColor = color;

        if (!isEnabled()) {
            if (SystemUtil.isDarkMode()) {
                color = Color.DKGRAY;
            } else {
                color = Color.LTGRAY;
            }
        }

        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, color}
        );
        drawable.setShape(GradientDrawable.OVAL);
        colorView.setBackground(drawable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        setPreviewColor(enabled ? getPreviewColor() : Color.GRAY);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        colorView = findViewById(R.id.color_widget);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        colorView.setId(View.generateViewId());

        LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, colorView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        container.setOnClickListener(l);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.selectedColor = selectedColor;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState ss)) {
            super.onRestoreInstanceState(state);
            return;
        }

        super.onRestoreInstanceState(ss.getSuperState());

        setPreviewColor(ss.selectedColor);
    }

    private static class SavedState extends BaseSavedState {
        int selectedColor;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            selectedColor = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(selectedColor);
        }

        public static final Creator<SavedState> CREATOR = new Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
