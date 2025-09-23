package me.jfenn.colorpickerdialog.views.picker;

import static me.jfenn.colorpickerdialog.utils.MiscUtil.setProgressBarDrawable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSeekBar;

import me.jfenn.colorpickerdialog.R;
import me.jfenn.colorpickerdialog.utils.ColorUtils;

public class RGBPickerView extends PickerView {

    private AppCompatSeekBar red, green, blue;
    private TextView redInt, greenInt, blueInt;
    private boolean isTrackingTouch;

    public RGBPickerView(Context context) {
        super(context);
    }

    public RGBPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RGBPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public RGBPickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.colorpicker_layout_rgb_picker, this);
        red = findViewById(R.id.red);
        redInt = findViewById(R.id.redInt);
        green = findViewById(R.id.green);
        greenInt = findViewById(R.id.greenInt);
        blue = findViewById(R.id.blue);
        blueInt = findViewById(R.id.blueInt);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getId() == R.id.red) {
                    redInt.setText(String.format("%s", i));
                } else if (seekBar.getId() == R.id.green) {
                    greenInt.setText(String.format("%s", i));
                } else if (seekBar.getId() == R.id.blue) {
                    blueInt.setText(String.format("%s", i));
                }
                onColorPicked();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
            }
        };

        red.setOnSeekBarChangeListener(listener);
        green.setOnSeekBarChangeListener(listener);
        blue.setOnSeekBarChangeListener(listener);

        int redColor = ColorUtils.fromAttrRes(getContext(), R.attr.redColor, R.color.colorPickerDialog_red);
        int greenColor = ColorUtils.fromAttrRes(getContext(), R.attr.greenColor, R.color.colorPickerDialog_green);
        int blueColor = ColorUtils.fromAttrRes(getContext(), R.attr.blueColor, R.color.colorPickerDialog_blue);

        GradientDrawable redDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{redColor, redColor}
        );
        setProgressBarDrawable(red, redDrawable, redColor);

        GradientDrawable greenDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{greenColor, greenColor}
        );
        setProgressBarDrawable(green, greenDrawable, greenColor);

        GradientDrawable blueDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{blueColor, blueColor}
        );
        setProgressBarDrawable(blue, blueDrawable, blueColor);
    }

    @Override
    protected SavedState newState(@Nullable Parcelable parcelable) {
        return new SavedState(parcelable);
    }

    @Override
    public void setColor(int color, boolean animate) {
        super.setColor(color, animate);
        SeekBar[] bars = new SeekBar[]{red, green, blue};
        int[] offsets = new int[]{16, 8, 0};
        for (int i = 0; i < bars.length; i++) {
            int value = (color >> offsets[i]) & 0xFF;
            if (animate && !isTrackingTouch) {
                ObjectAnimator animator = ObjectAnimator.ofInt(bars[i], "progress", value);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            } else {
                bars[i].setProgress(value);
            }
        }
    }

    @Override
    public int getColor() {
        return Color.argb(getColorAlpha(), red.getProgress(), green.getProgress(), blue.getProgress());
    }

    @NonNull
    @Override
    public String getName() {
        return getContext().getString(R.string.colorPickerDialog_rgb);
    }

    @Override
    public boolean isTrackingTouch() {
        return true;
    }
}
