package com.drdisagree.colorblendr.utils;

import android.content.res.Configuration;

import com.drdisagree.colorblendr.ColorBlendr;

public class SystemUtil {

    public static boolean isDarkMode() {
        return (ColorBlendr.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }
}
