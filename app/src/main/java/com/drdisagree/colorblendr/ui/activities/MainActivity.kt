package com.drdisagree.colorblendr.ui.activities

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.Insets
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.ADB_IP
import com.drdisagree.colorblendr.data.common.Constant.ADB_PAIRING_PORT
import com.drdisagree.colorblendr.data.common.Constant.ADB_PAIR_NOTIFICATION
import com.drdisagree.colorblendr.data.common.Constant.ADB_SEARCH_NOTIFICATION
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbMode
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.ui.compose.navigation.AppNavHost
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.parcelable
import com.drdisagree.colorblendr.utils.colors.PreviewResourcesOverride
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperColorUtil.updateWallpaperColorList
import com.drdisagree.colorblendr.utils.wifiadb.AdbPairingNotificationWorker
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {

    private val timeoutHandler: Handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private val colorsViewModel: ColorsViewModel by viewModels()
    private val stylesViewModel: StylesViewModel by viewModels {
        viewModelFactory {
            initializer { StylesViewModel(Utilities.getCustomStyleRepository()) }
        }
    }
    private val colorPaletteViewModel: ColorPaletteViewModel by viewModels()
    private var initSuccess by mutableStateOf<Boolean?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        setupEdgeToEdge()

        // colorblendr://theme/<id> deep link; anything else on data is a
        // backup file to restore.
        val intentData = intent?.data ?: intent?.parcelable<Uri>("data")
        val deepLinkThemeId = intentData
            ?.takeIf { it.scheme == "colorblendr" && it.host == "theme" }
            ?.lastPathSegment
        val restoreUri = if (deepLinkThemeId == null) intentData else null
        intent?.removeExtra("data")

        splashScreen.setKeepOnScreenCondition { initSuccess == null }

        if (savedInstanceState?.containsKey(KEY_INIT_SUCCESS) == true) {
            initSuccess = savedInstanceState.getBoolean(KEY_INIT_SUCCESS)
        } else {
            bootstrap()
        }

        // View-based UI (MDC dialogs, color picker) follows the preview via
        // runtime resource overrides.
        lifecycleScope.launch {
            PreviewController.previewColors.collectLatest { preview ->
                // Debounced: rebuilding the resources table on every slider
                // tick is expensive and dialogs only need the settled value.
                if (preview != null) delay(150.milliseconds)
                PreviewResourcesOverride.apply(this@MainActivity, preview)
            }
        }

        setContent {
            val success = initSuccess ?: return@setContent

            ColorBlendrTheme {
                AppNavHost(
                    success = success,
                    restoreUri = restoreUri,
                    deepLinkThemeId = deepLinkThemeId,
                    colorsViewModel = colorsViewModel,
                    stylesViewModel = stylesViewModel,
                    colorPaletteViewModel = colorPaletteViewModel
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        initSuccess?.let { outState.putBoolean(KEY_INIT_SUCCESS, it) }
    }

    private fun bootstrap() {
        lifecycleScope.launch(Dispatchers.IO) {
            val success = AtomicBoolean(false)
            val countDownLatch = CountDownLatch(1)

            handleInitialization(success, countDownLatch)

            try {
                countDownLatch.await()
            } catch (_: InterruptedException) {
            }

            withContext(Dispatchers.Main) {
                initSuccess = success.get()
            }
        }
    }

    private fun handleInitialization(
        success: AtomicBoolean,
        countDownLatch: CountDownLatch
    ) {
        if (!isFirstRun() && !isWorkMethodUnknown()) {
            when {
                isRootMode() -> {
                    RootConnectionProvider.builder(applicationContext)
                        .onSuccess {
                            lifecycleScope.launch(Dispatchers.IO) {
                                updateWallpaperColorList(applicationContext)
                                updateFabricatedAppList(applicationContext)
                                success.set(true)
                                countDownLatch.countDown()
                            }
                        }
                        .onFailure {
                            success.set(false)
                            countDownLatch.countDown()
                        }
                        .run()
                }

                isShizukuMode() -> {
                    if (isShizukuAvailable && hasShizukuPermission()) {
                        ShizukuUtil.bindUserService(
                            getUserServiceArgs(ShizukuConnection::class.java),
                            ShizukuConnectionProvider.serviceConnection
                        )
                        success.set(true)
                    } else {
                        success.set(false)
                    }
                    countDownLatch.countDown()
                }

                isWirelessAdbMode() -> {
                    if (WifiAdbShell.isMyDeviceConnected()) {
                        success.set(true)
                        countDownLatch.countDown()
                    } else {
                        WifiAdbShell.autoConnect(object : WifiAdbShell.ConnectionListener {
                            override fun onConnectionSuccess() {
                                success.set(true)
                                countDownLatch.countDown()
                            }

                            override fun onConnectionFailed() {
                                success.set(false)
                                countDownLatch.countDown()
                            }
                        })
                    }
                }

                else -> {
                    success.set(false)
                    countDownLatch.countDown()
                }
            }
        } else {
            success.set(false)
            countDownLatch.countDown()
        }
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            resources.configuration.screenWidthDp < 600
        ) {
            val viewGroup: ViewGroup = window.decorView.findViewById(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup) { v: View, windowInsets: WindowInsetsCompat ->
                val insets: Insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or
                                WindowInsetsCompat.Type.displayCutout()
                    )
                val params: MarginLayoutParams = v.layoutParams as MarginLayoutParams
                v.setPadding(
                    params.leftMargin + insets.left,
                    0,
                    params.rightMargin + insets.right,
                    0
                )
                params.topMargin = 0
                params.bottomMargin = 0
                v.layoutParams = params
                windowInsets
            }
        }
    }

    fun pairThisDevice() {
        showPairingNotification(true)
        startPairingCodeSearch()
    }

    private fun startPairingCodeSearch() {
        WifiAdbShell.getPairingAddress(
            object : WifiAdbShell.IpAddressListener {
                override fun onPortDetected(ip: String?, port: Int) {
                    if (ip != null && port != -1) {
                        Prefs.putString(ADB_IP, ip)
                        Prefs.putString(ADB_PAIRING_PORT, port.toString())
                        showPairingNotification()
                    }
                }
            }
        )
        startTimeout()
    }

    private fun showPairingNotification(isSearching: Boolean = false) {
        val workManager = WorkManager.getInstance(this)

        val workRequest = OneTimeWorkRequest
            .Builder(AdbPairingNotificationWorker::class.java)
            .setInputData(
                Data.Builder()
                    .putString("message", if (isSearching) "searching" else null)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            if (isSearching) ADB_SEARCH_NOTIFICATION else ADB_PAIR_NOTIFICATION,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // We run a timeout after which the pairing code searching will stop
    private fun startTimeout() {
        cancelTimeout()

        timeoutRunnable = Runnable {
            val workManager = WorkManager.getInstance(this)
            workManager.getWorkInfosForUniqueWork(ADB_SEARCH_NOTIFICATION)
                .get() // This blocks on background thread; should be inside a coroutine or background thread
                .firstOrNull()
                ?.let { workInfo ->
                    if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                        Toast.makeText(
                            this,
                            getString(R.string.adb_detection_timeout),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            workManager.cancelUniqueWork(ADB_SEARCH_NOTIFICATION)
        }

        timeoutHandler.postDelayed(timeoutRunnable!!, 3 * 60 * 1000) // 3 minutes
    }

    private fun cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable!!)
        }
    }

    override fun onDestroy() {
        cancelTimeout()

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES ||
            newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        ) {
            recreate()
            colorsViewModel.refreshData()
            stylesViewModel.refreshData()
            colorPaletteViewModel.refreshData()
        }
    }

    companion object {
        private const val KEY_INIT_SUCCESS = "initSuccess"
    }
}
