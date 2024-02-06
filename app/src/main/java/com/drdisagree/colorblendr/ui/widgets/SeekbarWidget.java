package com.drdisagree.colorblendr.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drdisagree.colorblendr.R;
import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.util.Objects;

public class SeekbarWidget extends RelativeLayout {

    private MaterialCardView container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private SeekBar seekBar;
    private ImageView resetIcon;
    private String valueFormat;
    private int defaultValue;
    private float outputScale = 1f;
    private boolean isDecimalFormat = false;
    private String decimalFormat = "#.#";
    private OnLongClickListener resetClickListener;

    public SeekbarWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SeekbarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SeekbarWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_seekbar, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekbarWidget);
        valueFormat = typedArray.getString(R.styleable.SeekbarWidget_progressFormat);
        defaultValue = typedArray.getInt(R.styleable.SeekbarWidget_seekbarDefaultProgress, Integer.MAX_VALUE);
        setTitle(typedArray.getString(R.styleable.SeekbarWidget_titleText));
        setSeekbarMinProgress(typedArray.getInt(R.styleable.SeekbarWidget_seekbarMinProgress, 0));
        setSeekbarMaxProgress(typedArray.getInt(R.styleable.SeekbarWidget_seekbarMaxProgress, 100));
        setSeekbarProgress(typedArray.getInt(
                R.styleable.SeekbarWidget_seekbarProgress,
                typedArray.getInt(R.styleable.SeekbarWidget_seekbarDefaultProgress, 50)
        ));
        isDecimalFormat = typedArray.getBoolean(R.styleable.SeekbarWidget_isDecimalFormat, false);
        decimalFormat = typedArray.getString(R.styleable.SeekbarWidget_decimalFormat);
        outputScale = typedArray.getFloat(R.styleable.SeekbarWidget_outputScale, 1f);
        typedArray.recycle();

        if (valueFormat == null) {
            valueFormat = "";
        }

        if (decimalFormat == null) {
            decimalFormat = "#.#";
        }

        setSelectedProgress();
        handleResetVisibility();
        setOnSeekbarChangeListener(null);
        setResetClickListener(null);
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSelectedProgress() {
        summaryTextView.setText(
                (valueFormat.isBlank() || valueFormat.isEmpty() ?
                        getContext().getString(
                                R.string.opt_selected1,
                                String.valueOf(
                                        !isDecimalFormat ?
                                                (int) (seekBar.getProgress() / outputScale) :
                                                new DecimalFormat(decimalFormat)
                                                        .format(seekBar.getProgress() / outputScale)
                                )
                        ) :
                        getContext().getString(
                                R.string.opt_selected2,
                                !isDecimalFormat ?
                                        String.valueOf((int) seekBar.getProgress()) :
                                        new DecimalFormat(decimalFormat)
                                                .format(seekBar.getProgress() / outputScale),
                                valueFormat
                        )
                )
        );
    }

    public int getSeekbarProgress() {
        return (int) seekBar.getProgress();
    }

    public void setSeekbarProgress(int value) {
        seekBar.setProgress(value);
        setSelectedProgress();
        handleResetVisibility();
    }

    public void setSeekbarMinProgress(int value) {
        seekBar.setMin(value);
    }

    public void setSeekbarMaxProgress(int value) {
        seekBar.setMax(value);
    }

    public void setIsDecimalFormat(boolean isDecimalFormat) {
        this.isDecimalFormat = isDecimalFormat;
        setSelectedProgress();
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = Objects.requireNonNullElse(decimalFormat, "#.#");
        setSelectedProgress();
    }

    public void setOutputScale(float scale) {
        this.outputScale = scale;
        setSelectedProgress();
    }

    public void setOnSeekbarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        seekBar.setOnSeekBarChangeListener(listener);
    }

    public void setResetClickListener(OnLongClickListener listener) {
        resetClickListener = listener;

        resetIcon.setOnClickListener(v -> {
            if (defaultValue == Integer.MAX_VALUE) {
                return;
            }

            Toast.makeText(getContext(), R.string.long_press_to_reset, Toast.LENGTH_SHORT).show();
        });

        resetIcon.setOnLongClickListener(v -> {
            if (defaultValue == Integer.MAX_VALUE) {
                return false;
            }

            setSeekbarProgress(defaultValue);
            handleResetVisibility();
            notifyOnResetClicked(v);

            return true;
        });
    }

    public void resetSeekbar() {
        resetIcon.performLongClick();
    }

    private void notifyOnResetClicked(View v) {
        if (resetClickListener != null) {
            resetClickListener.onLongClick(v);
        }
    }

    private void handleResetVisibility() {
        if (defaultValue != Integer.MAX_VALUE && seekBar.getProgress() != defaultValue) {
            resetIcon.setVisibility(VISIBLE);
        } else {
            resetIcon.setVisibility(GONE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        resetIcon.setEnabled(enabled);
        seekBar.setEnabled(enabled);

        if (enabled) {
            titleTextView.setAlpha(1.0f);
            summaryTextView.setAlpha(0.8f);
        } else {
            titleTextView.setAlpha(0.6f);
            summaryTextView.setAlpha(0.4f);
        }
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        seekBar = findViewById(R.id.seekbar_widget);
        resetIcon = findViewById(R.id.reset);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        seekBar.setId(View.generateViewId());
        resetIcon.setId(View.generateViewId());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.seekbarProgress = seekBar.getProgress();

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState ss)) {
            super.onRestoreInstanceState(state);
            return;
        }

        super.onRestoreInstanceState(ss.getSuperState());

        seekBar.setProgress(ss.seekbarProgress);
        setSelectedProgress();
        handleResetVisibility();
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<>() {
            public SeekbarWidget.SavedState createFromParcel(Parcel in) {
                return new SeekbarWidget.SavedState(in);
            }

            public SeekbarWidget.SavedState[] newArray(int size) {
                return new SeekbarWidget.SavedState[size];
            }
        };
        int seekbarProgress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            seekbarProgress = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(seekbarProgress);
        }
    }
}
