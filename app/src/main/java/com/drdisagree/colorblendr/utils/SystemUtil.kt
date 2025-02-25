package com.drdisagree.colorblendr.utils

import android.content.pm.PackageManager
import android.content.res.Configuration
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext

object SystemUtil {

    val isDarkMode: Boolean
        get() = (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

    fun isAppInstalled(packageName: String): Boolean {
        try {
            appContext
                .packageManager
                .getPackageInfo(packageName, PackageManager.GET_META_DATA)
            return true
        } catch (ignored: PackageManager.NameNotFoundException) {
            return false
        }
    }
}
