package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ShizukuConnectionProvider {

    private val TAG: String = ShizukuConnectionProvider::class.java.simpleName
    var serviceProvider: IShizukuConnection? = null
    private var isServiceConnected = false

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            if (binder == null || !binder.pingBinder()) {
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

    val getServiceProvider: IShizukuConnection?
        get() = serviceProvider

    private fun bindServiceConnection() {
        if (isServiceConnected) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            bindUserService(
                getUserServiceArgs(ShizukuConnection::class.java),
                serviceConnection
            )
        }
    }
}
