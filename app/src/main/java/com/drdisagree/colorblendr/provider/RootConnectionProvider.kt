package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.extension.MethodInterface
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.RootConnection
import com.topjohnwu.superuser.ipc.RootService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RootConnectionProvider private constructor(private val context: Context?) :
    ServiceConnection {

    private var methodRunOnSuccess: MethodInterface? = null
    private var methodRunOnFailure: MethodInterface? = null

    private fun bindServiceConnection() {
        if (isServiceConnected) {
            mServiceConnectionTimer.countDown()
            return
        }

        Handler(Looper.getMainLooper()).post {
            RootService.bind(
                Intent(context, RootConnection::class.java),
                this
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

    class ServiceConnectionThread(private val instance: RootConnectionProvider) : Thread() {
        override fun run() {
            try {
                instance.bindServiceConnection()
                val success = mServiceConnectionTimer.await(10, TimeUnit.SECONDS)
                Handler(Looper.getMainLooper()).post(
                    if (success) SuccessRunnable(instance) else FailureRunnable(instance)
                )
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        appContext,
                        R.string.something_went_wrong,
                        Toast.LENGTH_LONG
                    ).show()
                }
                Log.e(TAG, "Error starting service connection", e)
            }
        }
    }

    private data class SuccessRunnable(val instance: RootConnectionProvider) : Runnable {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                if (instance.methodRunOnSuccess != null) {
                    instance.methodRunOnSuccess!!.run()
                }
            }
        }
    }

    private data class FailureRunnable(val instance: RootConnectionProvider) : Runnable {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                if (instance.methodRunOnFailure != null) {
                    instance.methodRunOnFailure!!.run()
                } else {
                    if (workingMethod == Const.WorkMethod.ROOT ||
                        Const.WORKING_METHOD == Const.WorkMethod.ROOT
                    ) {
                        Toast.makeText(
                            appContext,
                            R.string.root_service_not_found,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    class Builder(context: Context?) {
        private val instance = RootConnectionProvider(context)

        fun runOnSuccess(method: MethodInterface?): Builder {
            instance.methodRunOnSuccess = method
            return this
        }

        fun runOnFailure(method: MethodInterface?): Builder {
            instance.methodRunOnFailure = method
            return this
        }

        fun run() {
            ServiceConnectionThread(instance).start()
        }
    }

    companion object {
        private val TAG: String = RootConnectionProvider::class.java.simpleName
        var serviceProvider: IRootConnection? = null
        private var isServiceConnected = false
        private val mServiceConnectionTimer = CountDownLatch(1)

        @JvmStatic
        fun builder(context: Context?): Builder {
            return Builder(context)
        }

        val isNotConnected: Boolean
            get() = !isServiceConnected
    }
}
