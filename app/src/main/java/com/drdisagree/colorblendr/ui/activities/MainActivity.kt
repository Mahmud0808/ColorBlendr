package com.drdisagree.colorblendr.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isRootOrShizukuUnknown
import com.drdisagree.colorblendr.databinding.ActivityMainBinding
import com.drdisagree.colorblendr.service.RestartBroadcastReceiver.Companion.scheduleJob
import com.drdisagree.colorblendr.ui.fragments.HomeFragment
import com.drdisagree.colorblendr.ui.fragments.onboarding.OnboardingFragment
import com.drdisagree.colorblendr.utils.app.parcelable
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        setupEdgeToEdge()

        myFragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            if (isFirstRun() || isRootOrShizukuUnknown() ||
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
        } catch (ignored: Exception) {
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
            val tag: String = fragment.javaClass.getSimpleName()
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