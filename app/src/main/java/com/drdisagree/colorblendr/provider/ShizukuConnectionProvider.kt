package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.utils.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.ShizukuUtil.getUserServiceArgs

object ShizukuConnectionProvider {

    private val TAG: String = ShizukuConnectionProvider::class.java.simpleName
    var serviceProvider: IShizukuConnection? = null
    private var isServiceConnected = false

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            if (!binder.pingBinder()) {
                Log.w(TAG, "Service binder is null or not alive")
                return
            }

            serviceProvider = IShizukuConnection.Stub.asInterface(binder)
            isServiceConnected = true
            Log.i(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceProvider = null
            isServiceConnected = false
            Log.w(TAG, "Service disconnected")
            bindServiceConnection()
        }
    }

    val isNotConnected: Boolean
        get() = !isServiceConnected

    private fun bindServiceConnection() {
        if (isServiceConnected) {
            return
        }

        Handler(Looper.getMainLooper()).post {
            bindUserService(
                getUserServiceArgs(ShizukuConnection::class.java),
                serviceConnection
            )
        }
    }
}
