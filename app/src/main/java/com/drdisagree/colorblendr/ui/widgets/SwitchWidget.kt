package com.drdisagree.colorblendr.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.utils.SystemUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SwitchWidget extends RelativeLayout {

    private Context context;
    private MaterialCardView container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private ImageView iconImageView;
    private MaterialSwitch materialSwitch;
    private MaterialSwitch.OnCheckedChangeListener switchChangeListener;
    private BeforeSwitchChangeListener beforeSwitchChangeListener;
    private boolean isMasterSwitch;
    private String summaryOnText;
    private String summaryOffText;

    public SwitchWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SwitchWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwitchWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        inflate(context, R.layout.view_widget_switch, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchWidget);
        setTitle(typedArray.getString(R.styleable.SwitchWidget_titleText));
        setSummary(typedArray.getString(R.styleable.SwitchWidget_summaryText));
        int icon = typedArray.getResourceId(R.styleable.SwitchWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.SwitchWidget_iconSpaceReserved, false);
        isMasterSwitch = typedArray.getBoolean(R.styleable.SwitchWidget_isMasterSwitch, false);
        summaryOnText = typedArray.getString(R.styleable.SwitchWidget_summaryOnText);
        summaryOffText = typedArray.getString(R.styleable.SwitchWidget_summaryOffText);
        setSwitchChecked(typedArray.getBoolean(R.styleable.SwitchWidget_isChecked, false));
        updateSummary();
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        container.setOnClickListener(v -> {
            if (materialSwitch.isEnabled()) {
                materialSwitch.toggle();
            }
        });

        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (materialSwitch.isEnabled()) {
                if (beforeSwitchChangeListener != null) {
                    beforeSwitchChangeListener.beforeSwitchChanged();
                }

                updateSummary();
                if (switchChangeListener != null) {
                    switchChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
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

    public void setIcon(int icon) {
        iconImageView.setImageResource(icon);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIconVisibility(int visibility) {
        iconImageView.setVisibility(visibility);
    }

    public boolean isSwitchChecked() {
        return materialSwitch.isChecked();
    }

    public void setSwitchChecked(boolean isChecked) {
        materialSwitch.setChecked(isChecked);
        if (switchChangeListener != null) {
            switchChangeListener.onCheckedChanged(materialSwitch, isChecked);
        }
    }

    private void updateSummary() {
        if (summaryOnText == null || summaryOffText == null) {
            return;
        }

        boolean isChecked = isSwitchChecked();

        if (isChecked) {
            setSummary(summaryOnText);
        } else {
            setSummary(summaryOffText);
        }

        if (isMasterSwitch) {
            container.setCardBackgroundColor(getCardBackgroundColor(isChecked));
            iconImageView.setColorFilter(getIconColor(isChecked), PorterDuff.Mode.SRC_IN);
            titleTextView.setTextColor(getTextColor(isChecked));
            summaryTextView.setTextColor(getTextColor(isChecked));
        }
    }

    private @ColorInt int getCardBackgroundColor(boolean isSelected) {
        return isSelected ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer) :
                ColorUtils.setAlphaComponent(
                        MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimaryContainer),
                        64
                );
    }

    private @ColorInt int getIconColor(boolean isSelected) {
        return isSelected ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary) :
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
    }

    private @ColorInt int getTextColor(boolean isSelected) {
        return isSelected ?
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimaryContainer) :
                MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
    }

    public void setSwitchChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        switchChangeListener = listener;
    }

    public void setBeforeSwitchChangeListener(BeforeSwitchChangeListener listener) {
        beforeSwitchChangeListener = listener;
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
            summaryTextView.setAlpha(0.8f);
        } else {
            if (SystemUtil.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }

            titleTextView.setAlpha(0.6f);
            summaryTextView.setAlpha(0.4f);
        }

        container.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        materialSwitch.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(R.id.icon);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        materialSwitch = findViewById(R.id.switch_widget);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        materialSwitch.setId(View.generateViewId());

        LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, materialSwitch.getId());
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    public interface BeforeSwitchChangeListener {
        void beforeSwitchChanged();
    }
}
