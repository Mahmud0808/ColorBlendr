package com.drdisagree.colorblendr.utils.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import android.widget.Toast
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


object SystemUtil {

    val isDarkMode: Boolean
        get() = (appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

    fun isAppInstalled(packageName: String): Boolean {
        try {
            appContext
                .packageManager
                .getPackageInfo(packageName, PackageManager.GET_META_DATA)
            return true
        } catch (_: PackageManager.NameNotFoundException) {
            return false
        }
    }

    @Suppress("DEPRECATION")
    fun isConnectedToWifi(context: Context): Boolean {
        val wifiManager =
            context.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return false

        val wifiInfo = wifiManager.connectionInfo

        if (wifiInfo != null && wifiInfo.networkId != -1) {
            return true
        }

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false

        return connectivityManager.getNetworkCapabilities(activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    fun requestEnableWifi(context: Context) {
        context.startActivity(Intent(Settings.Panel.ACTION_WIFI))
    }

    fun openDeveloperOptions(context: Context) {
        val devOptionsIntent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
        }

        val packageManager = context.packageManager
        val canResolve = devOptionsIntent.resolveActivity(packageManager) != null

        if (!AppUtil.hasNotificationPermission(context)) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.grant_permission)
                .setMessage(R.string.notification_access_not_granted)
                .setNegativeButton(R.string.close, null)
                .setPositiveButton(R.string.ok) { _, _ ->
                    AppUtil.openAppNotificationSettings(context)
                }
                .show()
        } else if (canResolve) {
            context.startActivity(devOptionsIntent)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.developer_options_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}
