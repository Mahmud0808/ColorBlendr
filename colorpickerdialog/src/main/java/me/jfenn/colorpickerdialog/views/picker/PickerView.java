package me.jfenn.colorpickerdialog.views.picker;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.fragment.app.FragmentManager;

import me.jfenn.colorpickerdialog.R;

import java.util.Locale;

import me.jfenn.androidutils.seekbar.SeekBarUtils;
import me.jfenn.colorpickerdialog.interfaces.ActivityRequestHandler;
import me.jfenn.colorpickerdialog.interfaces.ActivityResultHandler;
import me.jfenn.colorpickerdialog.interfaces.OnColorPickedListener;
import me.jfenn.colorpickerdialog.interfaces.PickerTheme;
import me.jfenn.colorpickerdialog.utils.ColorUtils;

public abstract class PickerView<S extends PickerView.SavedState> extends LinearLayout implements OnColorPickedListener<PickerView>, ActivityRequestHandler {

    private OnColorPickedListener<PickerView> listener;
    private ActivityRequestHandler requestHandler;

    private TextView alphaInt;
    private AppCompatSeekBar alpha;
    private View alphaLayout;

    private boolean isTrackingTouch;

    public PickerView(Context context) {
        super(context);
        init();
        postInit();
    }

    public PickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        postInit();
    }

    public PickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        postInit();
    }

    @RequiresApi(21)
    public PickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
        postInit();
    }

    /**
     * Called by the constructor, used to initialize and inflate
     * the child views of the picker layout.
     */
    protected abstract void init();

    /**
     * Called after `init()`, used to set up child views that should be
     * present in *most* pickers, such as the "alpha" slider.
     */
    private void postInit() {
        alphaInt = findViewById(R.id.alphaInt);
        alpha = findViewById(R.id.alpha);
        alphaLayout = findViewById(R.id.alphaLayout);

        if (alpha != null) {
            alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    alphaInt.setText(String.format(Locale.getDefault(), "%.2f", i / 255f));
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
            });

            SeekBarUtils.setProgressBarColor(
                    alpha,
                    ColorUtils.fromAttr(getContext(), R.attr.neutralColor,
                            ColorUtils.fromAttrRes(getContext(), android.R.attr.textColorPrimary, R.color.colorPickerDialog_neutral))
            );
        }
    }

    protected abstract S newState(@Nullable Parcelable parcelable);

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return newState(super.onSaveInstanceState())
                .fromInstance(this);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (state instanceof SavedState)
            ((SavedState) state).toInstance(this);
    }

    /**
     * Set the picker's color.
     *
     * @param color   The picker's color.
     * @param animate Whether to animate changes in values.
     */
    public void setColor(@ColorInt int color, boolean animate) {
        setColorAlpha(Color.alpha(color), animate);
    }

    /**
     * Get the current color value.
     *
     * @return The current color value.
     */
    @ColorInt
    public abstract int getColor();

    /**
     * Set the picker's color. Changes to values will not be animated.
     *
     * @param color The picker's color.
     */
    public void setColor(@ColorInt int color) {
        setColor(color, false);
    }

    /**
     * Get the "name" of the type of picker view. For example, an RGBPickerView
     * would return the string "RGB".
     *
     * @return The "name" of the type of picker.
     */
    @NonNull
    public abstract String getName();

    /**
     * Determine whether the view is currently tracking a touch interaction.
     * This is useful for determining when the next color update will occur
     * and deciding whether to animate a value change.
     *
     * @return Whether the view is currently tracking a touch interaction.
     */
    public boolean isTrackingTouch() {
        return isTrackingTouch;
    }

    /**
     * Determine whether the color's alpha value can be modified.
     *
     * @return Whether the color's alpha value can be modified.
     */
    public boolean isAlphaEnabled() {
        return alphaLayout != null && alphaLayout.getVisibility() == View.VISIBLE;
    }

    /**
     * Set whether the color's alpha value can be changed.
     *
     * @param isAlpha Whether the color's alpha value can be changed.
     */
    public void setAlphaEnabled(boolean isAlpha) {
        if (alphaLayout == null)
            return;

        if (isAlpha) {
            alphaLayout.setVisibility(View.VISIBLE);
        } else {
            alphaLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Set the color's alpha, between 0-1 (inclusive).
     *
     * @param alpha   The color's alpha, between 0-1 (inclusive).
     * @param animate Whether to animate the change in values.
     */
    public void setColorAlpha(int alpha, boolean animate) {
        if (this.alpha == null)
            return;

        if (animate && !isTrackingTouch) {
            ObjectAnimator animator = ObjectAnimator.ofInt(this.alpha, "progress", alpha);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else {
            this.alpha.setProgress(alpha);
        }
    }

    /**
     * Gets the color's alpha, from 0-255.
     *
     * @return The color's alpha, from 0-255.
     */
    public int getColorAlpha() {
        return alpha != null ? alpha.getProgress() : 255;
    }

    /**
     * Set the color's alpha, from 0-255. Change in values
     * will not be animated.
     *
     * @param alpha The color's alpha, from 0-255.
     */
    public void setColorAlpha(int alpha) {
        setColorAlpha(alpha, false);
    }

    /**
     * Set an interface to receive updates to color values. This may
     * be called multiple times in succession if a slider is dragged
     * or animated; be wary of performance.
     *
     * @param listener An interface to receive color updates.
     */
    public void setListener(OnColorPickedListener listener) {
        this.listener = listener;
    }

    protected void onColorPicked() {
        onColorPicked(this, getColor());
    }

    @Override
    public void onColorPicked(@Nullable PickerView pickerView, int color) {
        if (listener != null)
            listener.onColorPicked(this, getColor());
    }

    /**
     * Set a permissions handler interface for this view to use
     * for permission requests.
     *
     * @param requestHandler The interface to pass permission
     *                       requests to.
     * @return "This" view instance, for method
     * chaining.
     */
    public PickerView withActivityRequestHandler(ActivityRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    @Override
    public void handlePermissionsRequest(ActivityResultHandler resultHandler, String... permissions) {
        if (requestHandler != null)
            requestHandler.handlePermissionsRequest(resultHandler, permissions);
    }

    @Override
    public void handleActivityRequest(ActivityResultHandler resultHandler, Intent intent) {
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            if (requestHandler != null)
                requestHandler.handleActivityRequest(resultHandler, intent);
        } else {
            // Handle the case where no activity is available
            Toast.makeText(getContext(), "No application available to pick an image", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public FragmentManager requestFragmentManager() {
        if (requestHandler != null)
            return requestHandler.requestFragmentManager();
        else return null;
    }

    @Nullable
    @Override
    public PickerTheme getPickerTheme() {
        if (requestHandler != null)
            return requestHandler.getPickerTheme();
        else return null;
    }

    /**
     * Determine whether the PickerView currently has a request handler.
     *
     * @return True if the view has a request handler.
     */
    public boolean hasActivityRequestHandler() {
        return requestHandler != null;
    }

    public static class SavedState<T extends PickerView> extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        protected SavedState(Parcelable superState) {
            super(superState);
        }

        protected SavedState(@Nullable Parcel in) {
            super(in);
        }

        public SavedState<T> fromInstance(T view) {
            return this;
        }

        public SavedState<T> toInstance(T view) {
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
