package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.rootModeSelected
import com.drdisagree.colorblendr.service.AutoStartService
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.RootConnection
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class RootConnectionProvider private constructor() {

    private var onSuccess: (() -> Unit)? = null
    private var onFailure: (() -> Unit)? = null

    private suspend fun connectService(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (isServiceConnected) return@withContext true
                val latch = mServiceConnectionTimer
                bindServiceConnection()
                val connected = latch.await(10, TimeUnit.SECONDS) && isServiceConnected
                if (!connected) binding.set(false)
                connected
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service connection", e)
                binding.set(false)
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
        @Volatile
        private var serviceProvider: IRootConnection? = null

        @Volatile
        private var isServiceConnected = false

        @Volatile
        private var mServiceConnectionTimer = CountDownLatch(1)

        private val binding = AtomicBoolean(false)

        private val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
                binding.set(false)

                if (binder == null || !binder.pingBinder()) {
                    Log.w(TAG, "Service binder is null or not alive")
                    return
                }

                serviceProvider = IRootConnection.Stub.asInterface(binder)
                isServiceConnected = true
                mServiceConnectionTimer.countDown()
                Log.i(TAG, "Service connected")

                if (isRootMode() && !AutoStartService.isServiceNotRunning) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching { serviceProvider?.setSystemUIRestartListener() }
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mServiceConnectionTimer = CountDownLatch(1)
                serviceProvider = null
                isServiceConnected = false
                Log.w(TAG, "Service disconnected")
                bindServiceConnection()
            }
        }

        private fun bindServiceConnection() {
            if (isServiceConnected || !binding.compareAndSet(false, true)) return

            CoroutineScope(Dispatchers.Main).launch {
                RootService.bind(Intent(appContext, RootConnection::class.java), connection)
            }
        }

        fun builder(): Builder {
            return Builder()
        }

        val isNotConnected: Boolean
            get() = !isServiceConnected

        val getServiceProvider: IRootConnection?
            get() = serviceProvider
    }

    class Builder {
        private val instance = RootConnectionProvider()

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
