package com.drdisagree.colorblendr.ui.fragments.onboarding.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem2Binding
import com.drdisagree.colorblendr.utils.app.AppUtil.hasStoragePermission

class OnboardingItem2Fragment : Fragment() {

    private lateinit var binding: FragmentOnboardingItem2Binding
    private var hasNotificationPermission = false
    private var hasMediaPermission = false
    private var hasStoragePermission = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem2Binding.inflate(inflater, container, false)

        initPermissionsAndViews()

        // Post notifications permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.postNotifications.setOnClickListener {
                if (hasNotificationPermission) return@setOnClickListener

                binding.postNotifications.isSelected = false
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            binding.postNotifications.visibility = View.GONE
        }

        // Read media images permission
        binding.readMediaImages.setOnClickListener {
            if (hasMediaPermission) return@setOnClickListener

            binding.readMediaImages.isSelected = false
            requestMediaPermission.launch(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES
                ) else arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }

        // All files access permission
        binding.allFilesAccess.setOnClickListener {
            binding.allFilesAccess.isSelected = false
            val intent = Intent()
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.setData(
                Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null
                )
            )
            requestAllFilesPermission.launch(intent)
        }

        return binding.root
    }

    private fun initPermissionsAndViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            hasMediaPermission = checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            hasNotificationPermission = true
            hasMediaPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        hasStoragePermission = hasStoragePermission()

        if (hasNotificationPermission) {
            binding.postNotifications.isSelected = true
        }

        if (hasMediaPermission) {
            binding.readMediaImages.isSelected = true
        }

        if (hasStoragePermission) {
            binding.allFilesAccess.isSelected = true
        }
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result: Boolean ->
        if (result) {
            binding.postNotifications.isSelected = true
            hasNotificationPermission = true
        } else {
            binding.postNotifications.isSelected = false
            hasNotificationPermission = false
        }
    }

    private val requestMediaPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        this.handleMediaPermissionsResult(result)
    }

    private fun handleMediaPermissionsResult(result: Map<String, Boolean>) {
        for ((_, value) in result) {
            if (!value) {
                binding.readMediaImages.isSelected = false
                hasMediaPermission = false
                return
            }

            binding.readMediaImages.isSelected = true
            hasMediaPermission = true
        }
    }

    private val requestAllFilesPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult? ->
        if (hasStoragePermission()) {
            binding.allFilesAccess.isSelected = true
            hasStoragePermission = true
        } else {
            binding.allFilesAccess.isSelected = false
            hasStoragePermission = false
        }
    }

    private fun checkSelfPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()

        initPermissionsAndViews()
    }
}