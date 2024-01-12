package com.drdisagree.colorblendr.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.drdisagree.colorblendr.ColorBlendr;

public class SystemUtil {

    public static boolean isDarkMode() {
        return (ColorBlendr.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isAppInstalled(String package_name) {
        try {
            ColorBlendr.getAppContext()
                    .getPackageManager()
                    .getPackageInfo(package_name, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static int getScreenRotation(Context context) {

        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        return switch (display.getRotation()) {
            case Surface.ROTATION_0 -> 0;
            case Surface.ROTATION_90 -> 90;
            case Surface.ROTATION_180 -> 180;
            case Surface.ROTATION_270 -> 270;
            default -> -1;
        };
    }
}
