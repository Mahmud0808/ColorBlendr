package com.drdisagree.colorblendr.ui.fragments.onboarding.pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem3Binding

class OnboardingItem3Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem3Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false)

        binding.batteryOptimization.setOnClickListener {
            binding.batteryOptimization.isSelected = false

            if (!isBatteryOptimizationDisabled()) {
                requestDisableBatteryOptimization()
            }
        }

        updateUI()

        return binding.getRoot()
    }

    override fun onResume() {
        super.onResume()

        updateUI()
    }

    private fun isBatteryOptimizationDisabled(): Boolean {
        return (requireContext()
            .getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    @SuppressLint("BatteryLife")
    private fun requestDisableBatteryOptimization() {
        batteryOptimizationLauncher.launch(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:" + requireContext().packageName)
            }
        )
    }

    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        updateUI()
    }

    private fun updateUI() {
        val batteryOptimizationDisabled = isBatteryOptimizationDisabled()
        binding.batteryOptimization.isSelected = batteryOptimizationDisabled
        binding.batteryView.setBatteryImageViewColor(batteryOptimizationDisabled)
    }

    private fun ImageView.setBatteryImageViewColor(selected: Boolean) {
        val context = requireContext()
        val typedValue = TypedValue()
        val theme = context.theme
        @ColorInt val backgroundColor: Int
        @ColorInt val foregroundColor: Int
        val isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

        if (selected) {
            theme.resolveAttribute(
                if (isDarkMode) {
                    com.google.android.material.R.attr.colorSurfaceContainerHigh
                } else {
                    com.google.android.material.R.attr.colorSurfaceContainerHighest
                },
                typedValue,
                true
            )
            backgroundColor = typedValue.data

            theme.resolveAttribute(
                com.google.android.material.R.attr.colorPrimaryVariant,
                typedValue,
                true
            )
            foregroundColor = typedValue.data
        } else {
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorErrorContainer,
                typedValue,
                true
            )
            backgroundColor = typedValue.data

            theme.resolveAttribute(
                com.google.android.material.R.attr.colorPrimaryVariant,
                typedValue,
                true
            )
            foregroundColor = typedValue.data
        }

        setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
        foreground.setTintList(ColorStateList.valueOf(foregroundColor))
    }
}