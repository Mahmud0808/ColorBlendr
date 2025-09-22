package com.drdisagree.colorblendr

import android.app.Application
import android.content.Context
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.IRootConnection
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.google.android.material.color.DynamicColors
import io.github.muntashirakon.adb.PRNGFixes
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.ref.WeakReference

class ColorBlendr : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        contextReference = WeakReference(applicationContext)
        DynamicColors.applyToActivitiesIfAvailable(this)
        PRNGFixes.apply()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        HiddenApiBypass.addHiddenApiExemptions("L")
    }

    companion object {
        private lateinit var instance: ColorBlendr
        private lateinit var contextReference: WeakReference<Context>

        val appContext: Context
            get() {
                if (!this::contextReference.isInitialized || contextReference.get() == null) {
                    contextReference = WeakReference(
                        getInstance().applicationContext
                    )
                }
                return contextReference.get()!!
            }

        private fun getInstance(): ColorBlendr {
            if (!this::instance.isInitialized) {
                instance = ColorBlendr()
            }
            return instance
        }

        val rootConnection: IRootConnection?
            get() = RootConnectionProvider.getServiceProvider

        val shizukuConnection: IShizukuConnection?
            get() = ShizukuConnectionProvider.getServiceProvider
    }
}