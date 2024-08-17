package com.drdisagree.colorblendr.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

public class SelectableViewWidget extends RelativeLayout {

    private Context context;
    private MaterialCardView container;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ImageView iconImageView;
    private OnClickListener onClickListener;

    public SelectableViewWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SelectableViewWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SelectableViewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        inflate(context, R.layout.view_widget_selectable, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SelectableViewWidget);
        setTitle(typedArray.getString(R.styleable.SelectableViewWidget_titleText));
        setDescription(typedArray.getString(R.styleable.SelectableViewWidget_descriptionText));
        setSelected(typedArray.getBoolean(R.styleable.SelectableViewWidget_isSelected, false));
        typedArray.recycle();

        container.setOnClickListener(v -> {
            if (onClickListener != null && !isSelected()) {
                setSelected(true);
                onClickListener.onClick(v);
            }
        });

        updateViewOnOrientation();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDescription(int descriptionResId) {
        descriptionTextView.setText(descriptionResId);
    }

    public void setDescription(String description) {
        descriptionTextView.setText(description);
    }

    public boolean isSelected() {
        return iconImageView.getAlpha() == 1.0f;
    }

    public void setSelected(boolean isSelected) {
        iconImageView.setAlpha(isSelected ? 1.0f : 0.2f);
        iconImageView.setColorFilter(getIconColor(), PorterDuff.Mode.SRC_IN);
        iconImageView.setImageResource(isSelected ? R.drawable.ic_checked_filled : R.drawable.ic_checked_outline);
        container.setCardBackgroundColor(getCardBackgroundColor());
        container.setStrokeWidth(isSelected ? 0 : 2);
        titleTextView.setTextColor(getTextColor(isSelected));
        descriptionTextView.setTextColor(getTextColor(isSelected));
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = getContext().obtainStyledAttributes(
                    typedValue.data,
                    new int[]{com.google.android.material.R.attr.colorPrimary}
            );
            int color = a.getColor(0, 0);
            a.recycle();

            iconImageView.setImageTintList(ColorStateList.valueOf(color));

            titleTextView.setAlpha(1.0f);
            descriptionTextView.setAlpha(0.8f);
        } else {
            if (SystemUtil.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }

            titleTextView.setAlpha(0.6f);
            descriptionTextView.setAlpha(0.4f);
        }

        container.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        descriptionTextView.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(R.id.icon);
        titleTextView = findViewById(R.id.title);
        descriptionTextView = findViewById(R.id.description);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        descriptionTextView.setId(View.generateViewId());
    }

    private @ColorInt int getCardBackgroundColor() {
        return isSelected() ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer) :
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceContainer);
    }

    private @ColorInt int getIconColor() {
        return isSelected() ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary) :
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
    }

    private @ColorInt int getTextColor(boolean isSelected) {
        return isSelected ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimaryContainer) :
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
    }

    private void updateViewOnOrientation() {
        Configuration config = ColorBlendr.getAppContext().getResources().getConfiguration();
        boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLandscape) {
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

            boolean isSmallHeightDevice = screenWidth >= screenHeight * 1.8;

            if (isSmallHeightDevice) {
                container.setMinimumHeight(0);
                descriptionTextView.setVisibility(TextView.GONE);
            }
        } else {
            int minHeightInDp = 100;
            int minHeightInPixels = (int) (minHeightInDp * context.getResources().getDisplayMetrics().density);
            container.setMinimumHeight(minHeightInPixels);
            descriptionTextView.setVisibility(TextView.VISIBLE);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.isSelected = isSelected();

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState ss)) {
            super.onRestoreInstanceState(state);
            return;
        }

        super.onRestoreInstanceState(ss.getSuperState());

        setSelected(ss.isSelected);
        updateViewOnOrientation();
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean isSelected;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isSelected = in.readBoolean();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBoolean(isSelected);
        }
    }
}
