package me.jfenn.colorpickerdialog.interfaces;

import androidx.annotation.StyleRes;

public interface PickerTheme {

    @StyleRes
    int requestTheme();

    int requestCornerRadiusPx();

    boolean requestRetainInstance();

}
