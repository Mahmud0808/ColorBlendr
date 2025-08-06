package me.jfenn.colorpickerdialog.interfaces;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

public interface ActivityRequestHandler {

    void handlePermissionsRequest(ActivityResultHandler resultHandler, String... permissions);

    void handleActivityRequest(ActivityResultHandler resultHandler, Intent intent);

    @Nullable
    FragmentManager requestFragmentManager();

    @Nullable
    PickerTheme getPickerTheme();

}
