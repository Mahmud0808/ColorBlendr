package me.jfenn.colorpickerdialog.dialogs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.viewpager.widget.ViewPager;

import me.jfenn.colorpickerdialog.R;
import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import me.jfenn.colorpickerdialog.adapters.ColorPickerPagerAdapter;
import me.jfenn.colorpickerdialog.utils.ArrayUtils;
import me.jfenn.colorpickerdialog.utils.ColorUtils;
import me.jfenn.colorpickerdialog.utils.DelayedInstantiation;
import me.jfenn.colorpickerdialog.views.color.SmoothColorView;
import me.jfenn.colorpickerdialog.views.picker.HSVPickerView;
import me.jfenn.colorpickerdialog.views.picker.PickerView;
import me.jfenn.colorpickerdialog.views.picker.PresetPickerView;
import me.jfenn.colorpickerdialog.views.picker.RGBPickerView;

public class ColorPickerDialog extends PickerDialog<ColorPickerDialog> {

    private static final String INST_KEY_ALPHA = "me.jfenn.colorpickerdialog.INST_KEY_ALPHA";
    private static final String INST_KEY_PRESETS = "me.jfenn.colorpickerdialog.INST_KEY_PRESETS";
    private static final String INST_KEY_PICKERS = "me.jfenn.colorpickerdialog.INST_KEY_PICKERS";

    private SmoothColorView colorView;
    private AppCompatEditText colorHex;
    private ColorPickerPagerAdapter slidersAdapter;

    @SuppressWarnings("rawtypes")
    private DelayedInstantiation[] pickers;

    private boolean isAlphaEnabled = true;
    private int[] presets = new int[0];

    private boolean shouldIgnoreNextHex = false;

    @Override
    protected void init() {
        withPickers();
    }

    @Override
    public String getTitle() {
        String title = super.getTitle();
        return title != null ? title : getString(R.string.colorPickerDialog_dialogName);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isAlphaEnabled = savedInstanceState.getBoolean(INST_KEY_ALPHA, isAlphaEnabled);

            int[] presets = savedInstanceState.getIntArray(INST_KEY_PRESETS);
            if (presets != null) this.presets = presets;

            String[] pickerClassNames = savedInstanceState.getStringArray(INST_KEY_PICKERS);
            if (pickerClassNames != null && pickerClassNames.length > 0) {
                pickers = new DelayedInstantiation[pickerClassNames.length];
                for (int i = 0; i < pickerClassNames.length; i++) {
                    try {
                        Class tClass = Class.forName(pickerClassNames[i]);
                        Constructor constructor = tClass.getConstructor(Context.class);
                        constructor.setAccessible(true);

                        pickers[i] = Objects.requireNonNull(DelayedInstantiation.from(tClass, Context.class))
                                .withInstantiator(new DelayedInstantiation.ConstructionInstantiator<PickerView>(constructor) {
                                    @Override
                                    public PickerView instantiate(Object... args) {
                                        PickerView view = super.instantiate(args);

                                        try {
                                            assert view != null;
                                            Method method = view.getClass().getDeclaredMethod("onRestoreInstanceState", Parcelable.class);
                                            method.setAccessible(true);
                                            method.invoke(view, new Object[]{null});
                                        } catch (Exception ignored) {
                                            // can't _really_ do anything here... rip, I guess
                                        }

                                        return view;
                                    }
                                });
                    } catch (Exception ignored) {
                        // TODO: exception handling
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INST_KEY_ALPHA, isAlphaEnabled);
        outState.putIntArray(INST_KEY_PRESETS, presets);

        String[] pickerClassNames = new String[pickers.length];
        for (int i = 0; i < pickers.length; i++) {
            pickerClassNames[i] = pickers[i].gettClassName();
        }

        outState.putStringArray(INST_KEY_PICKERS, pickerClassNames);
    }

    /**
     * Specify whether alpha values should be enabled. This parameter
     * defaults to true.
     *
     * @param isAlphaEnabled Whether alpha values are enabled.
     * @return "This" dialog instance, for method chaining.
     */
    public ColorPickerDialog withAlphaEnabled(boolean isAlphaEnabled) {
        this.isAlphaEnabled = isAlphaEnabled;
        return this;
    }

    /**
     * Enables the preset picker view and applies the passed presets. Passing
     * nothing will enable the picker view with the default preset values.
     *
     * @param presets The preset colors to use.
     * @return "This" dialog instance, for method chaining.
     */
    public ColorPickerDialog withPresets(@ColorInt int... presets) {
        this.presets = presets;

        DelayedInstantiation<PresetPickerView> presetPicker = getPicker(PresetPickerView.class);
        if (presetPicker == null) {
            pickers = ArrayUtils.push(pickers, Objects.requireNonNull(DelayedInstantiation.from(PresetPickerView.class, Context.class))
                    .withInstantiator(args -> new PresetPickerView((Context) args[0])
                            .withPresets(ColorPickerDialog.this.presets)));
        }

        return this;
    }

    /**
     * Add an unidentified picker view to the dialog, if it doesn't already
     * exist. This class is instantiated by the dialog, to keep the view's
     * Context consistent with the rest of the styled components.
     * <p>
     * If the picker view already exists in the dialog, this will throw an
     * error.
     *
     * @param pickerClass The class of the picker view to add.
     * @return "This" dialog instance, for method chaining.
     */
    @SuppressWarnings("rawtypes")
    public <T extends PickerView> ColorPickerDialog withPicker(Class<T> pickerClass) {
        DelayedInstantiation<T> picker = getPicker(pickerClass);
        if (picker == null) {
            try {
                picker = DelayedInstantiation.from(pickerClass, Context.class);
            } catch (Exception e) {
                return null;
            }

            pickers = ArrayUtils.push(pickers, picker);
        } else return null;

        return this;
    }

    /**
     * Determine whether a particular picker view is enabled, and return
     * it. If not, this will return null.
     *
     * @param pickerClass The class of the PickerView.
     * @return The view, if it is enabled; null if not.
     */
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends PickerView> DelayedInstantiation<T> getPicker(Class<T> pickerClass) {
        for (DelayedInstantiation<?> picker : pickers) {
            if (pickerClass.isAssignableFrom(picker.getClass())) {
                return (DelayedInstantiation<T>) picker;
            }
        }
        return null;
    }

    /**
     * Set the picker views used by the dialog. If this method is called with
     * no arguments, the default pickers will be used; an RGB and HSV picker.
     *
     * @param pickers The picker views to use.
     * @return "This" dialog instance, for method chaining.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ColorPickerDialog withPickers(Class... pickers) {
        if (pickers.length == 0) {
            this.pickers = new DelayedInstantiation[]{
                    DelayedInstantiation.from(RGBPickerView.class, Context.class),
                    DelayedInstantiation.from(HSVPickerView.class, Context.class)
            };
        } else {
            this.pickers = new DelayedInstantiation[pickers.length];
            for (int i = 0; i < pickers.length; i++) {
                this.pickers[i] = DelayedInstantiation.from(pickers[i], Context.class);
            }
        }

        return this;
    }

    /**
     * Clears the picker views used by dialog.
     * This is a workaround, if you want to show the presets tab as first tab
     * you can use withPickers(), but then you use the default colors, not the ones you've set.
     *
     * @return "This" dialog instance, for method chaining.
     * @see #withPickers
     */
    public ColorPickerDialog clearPickers() {
        this.pickers = new DelayedInstantiation[0];
        return this;
    }

    @Nullable
    @Override
    @SuppressWarnings("rawtypes")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.colorpicker_dialog_color_picker, container, false);

        colorView = v.findViewById(R.id.color);
        colorHex = v.findViewById(R.id.colorHex);
        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        ViewPager slidersPager = v.findViewById(R.id.slidersPager);

        Context context = new ContextThemeWrapper(getContext(), getTheme());
        PickerView[] pickers = new PickerView[this.pickers.length];

        for (int i = 0; i < pickers.length; i++) {
            pickers[i] = (PickerView) this.pickers[i].instantiate(context);
            if (pickers[i] != null && !pickers[i].hasActivityRequestHandler())
                pickers[i].withActivityRequestHandler(this);
        }

        slidersAdapter = new ColorPickerPagerAdapter(getContext(), pickers);
        slidersAdapter.setListener(this);
        slidersAdapter.setAlphaEnabled(isAlphaEnabled);
        slidersAdapter.setColor(getColor());

        slidersPager.setAdapter(slidersAdapter);
        slidersPager.addOnPageChangeListener(slidersAdapter);
        tabLayout.setupWithViewPager(slidersPager);

        colorHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Editable editable = colorHex.getText();
                if (editable != null && !shouldIgnoreNextHex) {
                    String str = editable.toString();

                    if (str.length() == (isAlphaEnabled ? 9 : 7)) {
                        try {
                            slidersAdapter.updateColor(Color.parseColor(str), true);
                        } catch (Exception ignored) {
                        }
                    }
                } else shouldIgnoreNextHex = false;
            }
        });

        v.findViewById(R.id.confirm).setOnClickListener(v1 -> confirm());

        v.findViewById(R.id.cancel).setOnClickListener(v12 -> dismiss());

        onColorPicked(null, getColor());

        return v;
    }

    @Override
    @SuppressWarnings("all")
    public void onColorPicked(@Nullable PickerView pickerView, @ColorInt int color) {
        super.onColorPicked(pickerView, color);
        colorView.setColor(color, pickerView != null && !pickerView.isTrackingTouch());

        shouldIgnoreNextHex = true;
        colorHex.setText(String.format(isAlphaEnabled ? "#%08X" : "#%06X", isAlphaEnabled ? color : (0xFFFFFF & color)));
        colorHex.clearFocus();

        int textColor = ColorUtils.isColorDark(ColorUtils.withBackground(color, Color.WHITE)) ? Color.WHITE : Color.BLACK;
        colorHex.setTextColor(textColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            colorHex.setBackgroundTintList(ColorStateList.valueOf(textColor));
    }
}
