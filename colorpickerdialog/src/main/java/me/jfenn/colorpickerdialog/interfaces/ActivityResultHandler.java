package me.jfenn.colorpickerdialog.interfaces;

import android.content.Intent;

public interface ActivityResultHandler {

    void onPermissionsResult(String[] permissions, int[] grantResults);

    void onActivityResult(int resultCode, Intent data);

}
