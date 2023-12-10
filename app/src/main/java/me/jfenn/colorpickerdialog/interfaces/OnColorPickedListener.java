package me.jfenn.colorpickerdialog.interfaces;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public interface OnColorPickedListener<T> {
    void onColorPicked(@Nullable T pickerView, @ColorInt int color);
}
