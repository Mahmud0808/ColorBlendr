package com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.interfaces;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public interface OnColorPickedListener<T> {
    void onColorPicked(@Nullable T pickerView, @ColorInt int color);
}
