package com.drdisagree.colorblendr.ui.widgets;

import static com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS;
import static com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.utils.ColorSchemeUtil;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class StylePreviewWidget extends RelativeLayout {

    private Context context;
    private MaterialCardView container;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private ViewGroup colorsContainer;
    private View accent1, accent2, neutral;
    private boolean isSelected = false;
    private OnClickListener onClickListener;
    private String styleName;
    private ColorSchemeUtil.MONET monetStyle;
    private ArrayList<ArrayList<Integer>> colorPalette;

    public StylePreviewWidget(Context context) {
        super(context);
        init(context, null);
    }

    public StylePreviewWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StylePreviewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        inflate(context, R.layout.view_widget_style_preview, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StylePreviewWidget);
        styleName = typedArray.getString(R.styleable.StylePreviewWidget_titleText);
        setTitle(styleName);
        setDescription(typedArray.getString(R.styleable.StylePreviewWidget_descriptionText));
        typedArray.recycle();

        setColorPreview();

        container.setOnClickListener(v -> {
            if (onClickListener != null && !isSelected()) {
                setSelected(true);
                onClickListener.onClick(v);
            }
        });
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDescription(int summaryResId) {
        descriptionTextView.setText(summaryResId);
    }

    public void setDescription(String summary) {
        descriptionTextView.setText(summary);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        container.setCardBackgroundColor(getCardBackgroundColor());
        container.setStrokeWidth(isSelected ? 0 : 2);
        titleTextView.setTextColor(getTextColor(isSelected));
        descriptionTextView.setTextColor(getTextColor(isSelected));
    }

    public void applyColorScheme() {
        RPrefs.putString(MONET_STYLE, styleName);
        OverlayManager.applyFabricatedColors(context);
    }

    private void setColorPreview() {
        new Thread(() -> {
            try {
                if (styleName == null) {
                    styleName = context.getString(R.string.monet_tonalspot);
                }

                monetStyle = ColorSchemeUtil.stringToEnumMonetStyle(
                        context,
                        styleName
                );

                colorPalette = ColorUtil.generateModifiedColors(
                        context,
                        monetStyle,
                        RPrefs.getInt(MONET_ACCENT_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_SATURATION, 100),
                        RPrefs.getInt(MONET_BACKGROUND_LIGHTNESS, 100),
                        RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false),
                        RPrefs.getBoolean(MONET_ACCURATE_SHADES, true)
                );

                new Handler(Looper.getMainLooper()).post(() -> {
                    boolean isDarkMode = SystemUtil.isDarkMode();

                    accent1.getBackground().setTint(colorPalette.get(0).get(5));
                    accent2.getBackground().setTint(colorPalette.get(2).get(5));
                    if (!isDarkMode) {
                        neutral.getBackground().setTint(colorPalette.get(3).get(1));
                        colorsContainer.getBackground().setTint(colorPalette.get(1).get(3));
                    } else {
                        neutral.getBackground().setTint(colorPalette.get(3).get(11));
                        colorsContainer.getBackground().setTint(colorPalette.get(1).get(9));
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        descriptionTextView = findViewById(R.id.summary);
        colorsContainer = findViewById(R.id.colors);
        accent1 = findViewById(R.id.accent1);
        accent2 = findViewById(R.id.accent2);
        neutral = findViewById(R.id.neutral);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        descriptionTextView.setId(View.generateViewId());
        colorsContainer.setId(View.generateViewId());
        accent1.setId(View.generateViewId());
        accent2.setId(View.generateViewId());
        neutral.setId(View.generateViewId());

        LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.END_OF, colorsContainer.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    private @ColorInt int getCardBackgroundColor() {
        return isSelected() ?
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorPrimaryContainer) :
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorSurfaceContainer);
    }

    private @ColorInt int getTextColor(boolean isSelected) {
        return isSelected ?
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorOnPrimaryContainer) :
                ColorUtil.getColorFromAttribute(context, com.google.android.material.R.attr.colorOnSurface);
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
        setColorPreview();
    }

    private static class SavedState extends BaseSavedState {
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

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
