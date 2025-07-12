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
import com.drdisagree.colorblendr.data.common.Constant.ADB_CONNECTING_PORT
import com.drdisagree.colorblendr.data.common.Constant.ADB_IP
import com.drdisagree.colorblendr.data.common.Constant.ADB_PAIRING_PORT
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.databinding.ActivityMainBinding
import com.drdisagree.colorblendr.service.RestartBroadcastReceiver.Companion.scheduleJob
import com.drdisagree.colorblendr.ui.fragments.HomeFragment
import com.drdisagree.colorblendr.ui.fragments.onboarding.OnboardingFragment
import com.drdisagree.colorblendr.utils.app.parcelable
import com.drdisagree.colorblendr.utils.wifiadb.AdbMdns
import com.drdisagree.colorblendr.utils.wifiadb.AdbMdns.AdbFoundCallback
import com.drdisagree.colorblendr.utils.wifiadb.AdbPairingNotificationWorker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var adbMdns: AdbMdns? = null
    private val timeoutHandler: Handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

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
                intent?.removeExtra("data")
            }
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
                v.setLayoutParams(params)
                windowInsets
            }
        }
    }

    fun pairThisDevice() {
        stopMdns()
        showSearchingNotification()
        startPairingCodeSearch()
    }

    private fun showSearchingNotification() {
        val workRequest = OneTimeWorkRequest.Builder(AdbPairingNotificationWorker::class.java)
            .setInputData(
                Data.Builder()
                    .putString("message", "Searching for Pairing Codes...")
                    .build()
            )
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "adb_searching_notification",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    // Start searching for wireless debugging pairing code
    private fun startPairingCodeSearch() {
        adbMdns = AdbMdns(
            this,
            object : AdbFoundCallback {
                override fun onPairingCodeDetected(ipAddress: String, port: Int) {
                    Prefs.putString(ADB_IP, ipAddress)
                    Prefs.putString(ADB_PAIRING_PORT, port.toString())
                    enterPairingCodeNotification()
                }

                override fun onConnectCodeDetected(ipAddress: String, port: Int) {
                    Prefs.putString(ADB_IP, ipAddress)
                    Prefs.putString(ADB_CONNECTING_PORT, port.toString())
                }
            }
        )

        adbMdns?.start()
        startTimeout() // Start the 3-minute timeout
    }

    // We run a timeout after which the pairing code searching will stop
    private fun startTimeout() {
        timeoutRunnable = Runnable {
            stopMdns()

            val workManager = WorkManager.getInstance(this)
            workManager.getWorkInfosForUniqueWork("adb_searching_notification")
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

            workManager.cancelUniqueWork("adb_searching_notification")
        }

        timeoutHandler.postDelayed(timeoutRunnable!!, 3 * 60 * 1000) // 3 minutes
    }

    private fun cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable!!)
        }
    }

    private fun stopMdns() {
        adbMdns?.stop()
        adbMdns = null
        cancelTimeout()
    }

    private fun enterPairingCodeNotification() {
        val workRequest =
            OneTimeWorkRequest.Builder(AdbPairingNotificationWorker::class.java).build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork("adb_pairing_notification", ExistingWorkPolicy.REPLACE, workRequest)
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
        super.onDestroy()

        stopMdns()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES ||
            newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
        ) {
            recreate()
        }
    }

    companion object {
        private lateinit var myFragmentManager: FragmentManager

        fun replaceFragment(fragment: Fragment, animate: Boolean) {
            val tag: String = fragment.javaClass.simpleName
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