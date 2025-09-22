package me.jfenn.colorpickerdialog.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import me.jfenn.colorpickerdialog.interfaces.OnColorPickedListener;
import me.jfenn.colorpickerdialog.views.picker.PickerView;

public class ColorPickerPagerAdapter extends HeightablePagerAdapter implements OnColorPickedListener<PickerView>, ViewPager.OnPageChangeListener {

    private final Context context;
    private final PickerView[] pickers;
    private OnColorPickedListener<PickerView> listener;
    @ColorInt
    private int color = Color.BLACK;
    private boolean isAlphaEnabled = true;
    private int position;

    public ColorPickerPagerAdapter(Context context, PickerView... pickers) {
        this.context = context;
        this.pickers = pickers;
    }

    /**
     * Specify a listener to receive updates when a new color is selected.
     *
     * @param listener The listener to receive updates.
     */
    public void setListener(OnColorPickedListener<PickerView> listener) {
        this.listener = listener;
    }

    /**
     * Specify an initial color for the picker(s) to use.
     *
     * @param color The initial color int.
     */
    public void setColor(@ColorInt int color) {
        this.color = color;
        if (pickers[position] != null)
            pickers[position].setColor(color);
    }

    /**
     * Specify whether alpha values should be enabled. This parameter
     * defaults to true.
     *
     * @param isAlphaEnabled Whether alpha values are enabled.
     */
    public void setAlphaEnabled(boolean isAlphaEnabled) {
        this.isAlphaEnabled = isAlphaEnabled;
    }

    /**
     * Update the color value used by the picker(s).
     *
     * @param color   The new color int.
     * @param animate Whether to animate the change in values.
     */
    public void updateColor(@ColorInt int color, boolean animate) {
        if (pickers[position] != null)
            pickers[position].setColor(color, animate);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        if (position >= 0 && position < pickers.length && pickers[position] != null) {
            PickerView picker = pickers[position];
            picker.setListener(this);
            picker.setAlphaEnabled(isAlphaEnabled);
            picker.setColor(color);
            view = picker;
        } else view = new View(context);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return pickers.length;
    }

    @Override
    public int getHeightAt(int position, int widthMeasureSpec, int heightMeasureSpec) {
        pickers[position].measure(widthMeasureSpec, heightMeasureSpec);
        return pickers[position].getMeasuredHeight();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pickers[position] != null ? pickers[position].getName() : "";
    }

    @Override
    public void onColorPicked(PickerView pickerView, @ColorInt int color) {
        this.color = color;
        if (listener != null)
            listener.onColorPicked(pickerView, color);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        if (pickers[position] != null)
            pickers[position].setColor(color);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
