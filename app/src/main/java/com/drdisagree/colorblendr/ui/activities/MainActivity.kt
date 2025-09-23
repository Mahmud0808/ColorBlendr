package com.drdisagree.colorblendr.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.databinding.ActivityMainBinding
import com.drdisagree.colorblendr.service.RestartBroadcastReceiver.Companion.scheduleJob
import com.drdisagree.colorblendr.ui.fragments.HomeFragment
import com.drdisagree.colorblendr.ui.fragments.onboarding.OnboardingFragment
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.parcelable
import com.drdisagree.colorblendr.utils.wifiadb.AdbPairingNotificationWorker
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val timeoutHandler: Handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private val colorsViewModel: ColorsViewModel by viewModels()
    private val stylesViewModel: StylesViewModel by viewModels()
    private val colorPaletteViewModel: ColorPaletteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        setupEdgeToEdge()

        myFragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            if (isFirstRun() || isWorkMethodUnknown() ||
                intent?.getBooleanExtra("success", false) == false
            ) {
                replaceFragment(OnboardingFragment(), false)
            } else {
                replaceFragment(
                    HomeFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("success", true)
                            if (intent?.hasExtra("data") == true) {
                                putParcelable("data", intent.parcelable("data"))
                            }
                        }
                    },
                    false
                )
            }
            intent?.removeExtra("success")
            intent?.removeExtra("data")
        }
    }

    private fun setupEdgeToEdge() {
        try {
            (findViewById<View>(R.id.appBarLayout) as AppBarLayout).statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(applicationContext)
        } catch (_: Exception) {
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (getResources().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val viewGroup: ViewGroup = window.decorView.findViewById(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup) { v: View, windowInsets: WindowInsetsCompat ->
                val insets: Insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        if (intent?.getBooleanExtra("success", false) == true) {
            scheduleJob(applicationContext)
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
        private lateinit var myFragmentManager: FragmentManager

        fun replaceFragment(fragment: Fragment, animate: Boolean) {
            val tag: String = fragment.javaClass.simpleName
            val existing = myFragmentManager.findFragmentByTag(tag)

            if (existing != null && existing.isVisible) {
                return // already showing, don’t replace
            }

            val fragmentTransaction: FragmentTransaction = myFragmentManager.beginTransaction()

            if (animate) {
                fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
            fragmentTransaction.replace(
                R.id.fragmentContainer,
                fragment
            )

            if (tag == HomeFragment::class.java.simpleName) {
                myFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else if (tag != OnboardingFragment::class.java.simpleName) {
                fragmentTransaction.addToBackStack(tag)
            }

            fragmentTransaction.commit()
        }
    }
}