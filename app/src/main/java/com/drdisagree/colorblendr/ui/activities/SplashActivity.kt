package com.drdisagree.colorblendr.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isRootOrShizukuUnknown
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.provider.RootConnectionProvider.Companion.builder
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.utils.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.WallpaperColorUtil.updateWallpaperColorList
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var keepShowing: Boolean = true

    private val runnable: Runnable = Runnable {
        val success = AtomicBoolean(false)
        val countDownLatch = CountDownLatch(1)

        handleInitialization(success, countDownLatch)

        try {
            countDownLatch.await()
        } catch (ignored: InterruptedException) {
        }

        startActivity(
            Intent(
                this@SplashActivity,
                MainActivity::class.java
            ).apply {
                putExtra("success", success.get())
                intent.data?.let { uri ->
                    putExtra("data", uri)
                    intent.removeExtra("data")
                }
            }
        )
        finish()
    }

    private fun handleInitialization(
        success: AtomicBoolean,
        countDownLatch: CountDownLatch
    ) {
        if (!isFirstRun() && !isRootOrShizukuUnknown()) {
            if (isRootMode()) {
                builder(appContext)
                    .onSuccess {
                        CoroutineScope(Dispatchers.IO).launch {
                            updateWallpaperColorList(applicationContext)
                            updateFabricatedAppList(applicationContext)
                            success.set(true)
                            keepShowing = false
                            countDownLatch.countDown()
                        }
                    }
                    .onFailure {
                        success.set(false)
                        keepShowing = false
                        countDownLatch.countDown()
                    }
                    .run()
            } else if (isShizukuMode()) {
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
            success.set(false)
            keepShowing = false
            countDownLatch.countDown()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application)
        splashScreen.setKeepOnScreenCondition { keepShowing }

        Thread(runnable).start()
    }
}
