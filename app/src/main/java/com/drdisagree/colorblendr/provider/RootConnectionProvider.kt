package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.rootModeSelected
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.RootConnection
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RootConnectionProvider private constructor(private val context: Context) : ServiceConnection {

    private var onSuccess: (() -> Unit)? = null
    private var onFailure: (() -> Unit)? = null

    private fun bindServiceConnection() {
        if (isServiceConnected) {
            mServiceConnectionTimer.countDown()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            RootService.bind(
                Intent(context, RootConnection::class.java),
                this@RootConnectionProvider
            )
        }
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
        if (binder == null || !binder.pingBinder()) {
            Log.w(TAG, "Service binder is null or not alive")
            return
        }

        serviceProvider = IRootConnection.Stub.asInterface(binder)
        isServiceConnected = true
        mServiceConnectionTimer.countDown()
        Log.i(TAG, "Service connected")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        serviceProvider = null
        isServiceConnected = false
        Log.w(TAG, "Service disconnected")
        bindServiceConnection()
    }

    private suspend fun connectService(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                bindServiceConnection()
                mServiceConnectionTimer.await(10, TimeUnit.SECONDS)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service connection", e)
                false
            }
        }
    }

    fun setOnSuccess(callback: (() -> Unit)?) {
        this.onSuccess = callback
    }

    fun setOnFailure(callback: (() -> Unit)?) {
        this.onFailure = callback
    }

    private fun handleSuccess() {
        CoroutineScope(Dispatchers.Main).launch {
            onSuccess?.invoke()
        }
    }

    private fun handleFailure() {
        CoroutineScope(Dispatchers.Main).launch {
            onFailure?.invoke() ?: run {
                if (isRootMode() || rootModeSelected()) {
                    Toast.makeText(
                        appContext,
                        R.string.root_service_not_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        private val TAG: String = RootConnectionProvider::class.java.simpleName
        private var serviceProvider: IRootConnection? = null
        private var isServiceConnected = false
        private val mServiceConnectionTimer = CountDownLatch(1)

        fun builder(context: Context): Builder {
            return Builder(context)
        }

        val isNotConnected: Boolean
            get() = !isServiceConnected

        val getServiceProvider: IRootConnection?
            get() = serviceProvider
    }

    class Builder(context: Context) {
        private val instance = RootConnectionProvider(context)

        fun onSuccess(callback: () -> Unit): Builder {
            instance.setOnSuccess(callback)
            return this
        }

        fun onFailure(callback: () -> Unit): Builder {
            instance.setOnFailure(callback)
            return this
        }

        fun run() {
            CoroutineScope(Dispatchers.IO).launch {
                val success = instance.connectService()

                withContext(Dispatchers.Main) {
                    if (success) {
                        instance.handleSuccess()
                    } else {
                        instance.handleFailure()
                    }
                }
            }
        }
    }
}
