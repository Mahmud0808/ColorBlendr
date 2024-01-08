package com.drdisagree.colorblendr.utils;

import android.content.pm.PackageManager;
import android.content.res.Configuration;

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
}
