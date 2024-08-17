package com.drdisagree.colorblendr.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.FIRST_RUN
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.extension.MethodInterface
import com.drdisagree.colorblendr.provider.RootConnectionProvider.Companion.builder
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.utils.FabricatedUtil.getAndSaveSelectedFabricatedApps
import com.drdisagree.colorblendr.utils.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.getAndSaveWallpaperColors
import com.google.android.material.color.DynamicColors
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var keepShowing: Boolean = true

    private val runnable: Runnable = Runnable {
        val success = AtomicBoolean(false)
        val countDownLatch = CountDownLatch(1)

        if (!getBoolean(FIRST_RUN, true) &&
            workingMethod != Const.WorkMethod.NULL
        ) {
            if (workingMethod == Const.WorkMethod.ROOT) {
                builder(appContext)
                    .runOnSuccess(object : MethodInterface() {
                        override fun run() {
                            getAndSaveWallpaperColors(applicationContext)
                            getAndSaveSelectedFabricatedApps(applicationContext)
                            success.set(true)
                            keepShowing = false
                            countDownLatch.countDown()
                        }
                    })
                    .runOnFailure(object : MethodInterface() {
                        override fun run() {
                            success.set(false)
                            keepShowing = false
                            countDownLatch.countDown()
                        }
                    })
                    .run()
            } else if (workingMethod == Const.WorkMethod.SHIZUKU) {
                if (isShizukuAvailable && hasShizukuPermission(this)) {
                    bindUserService(
                        getUserServiceArgs(ShizukuConnection::class.java),
                        ShizukuConnectionProvider.serviceConnection
                    )
                    success.set(true)
                } else {
                    success.set(false)
                }
                keepShowing = false
                countDownLatch.countDown()
            }
        } else {
            keepShowing = false
            countDownLatch.countDown()
        }

        try {
            countDownLatch.await()
        } catch (ignored: InterruptedException) {
        }
        val intent = Intent(
            this@SplashActivity,
            MainActivity::class.java
        )
        intent.putExtra("success", success.get())
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application)
        splashScreen.setKeepOnScreenCondition { keepShowing }

        Thread(runnable).start()
    }
}
