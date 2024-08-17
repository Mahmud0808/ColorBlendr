package com.drdisagree.colorblendr.service

import android.content.Context
import android.content.Intent
import android.util.Log

class ServiceLauncher {
    private fun setServiceIntent(context: Context) {
        if (serviceIntent == null) {
            serviceIntent = Intent(
                context,
                AutoStartService::class.java
            )
        }
    }

    fun launchService(context: Context?) {
        if (context == null) {
            return
        }

        setServiceIntent(context)

        if (AutoStartService.isServiceNotRunning) {
            Log.d(TAG, "launchService: Service is starting...")
            context.startForegroundService(serviceIntent)
        }
    }

    companion object {
        private val TAG: String = ServiceLauncher::class.java.simpleName
        private var serviceIntent: Intent? = null
    }
}
