package com.drdisagree.colorblendr.utils

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.data.common.Const
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import rikka.shizuku.Shizuku.UserServiceArgs
import rikka.shizuku.ShizukuProvider

object ShizukuUtil {
    val isShizukuAvailable: Boolean
        get() = Shizuku.pingBinder()

    fun hasShizukuPermission(context: Context): Boolean {
        if (!isShizukuAvailable) {
            return false
        }

        return if (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11()) {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } else {
            context.checkCallingOrSelfPermission(ShizukuProvider.PERMISSION) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestShizukuPermission(activity: ComponentActivity, callback: ShizukuPermissionCallback) {
        if (Shizuku.getVersion() >= 11 && !Shizuku.isPreV11()) {
            Shizuku.addRequestPermissionResultListener(object : OnRequestPermissionResultListener {
                override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                    Shizuku.removeRequestPermissionResultListener(this)
                    callback.onPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            })
            Shizuku.requestPermission(Const.SHIZUKU_PERMISSION_REQUEST_ID)
        } else {
            val permCallback = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted: Boolean -> callback.onPermissionResult(granted) }

            permCallback.launch(ShizukuProvider.PERMISSION)
        }
    }

    fun getUserServiceArgs(className: Class<*>): UserServiceArgs {
        return UserServiceArgs(
            ComponentName(
                BuildConfig.APPLICATION_ID,
                className.name
            )
        )
            .daemon(false)
            .processNameSuffix("user_service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)
    }

    fun bindUserService(userServiceArgs: UserServiceArgs, serviceConnection: ServiceConnection) {
        Shizuku.bindUserService(userServiceArgs, serviceConnection)
    }

    fun unbindUserService(userServiceArgs: UserServiceArgs, serviceConnection: ServiceConnection?) {
        Shizuku.unbindUserService(userServiceArgs, serviceConnection, true)
    }

    fun interface ShizukuPermissionCallback {
        fun onPermissionResult(granted: Boolean)
    }
}
