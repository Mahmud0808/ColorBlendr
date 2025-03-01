package com.drdisagree.colorblendr.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.databinding.FragmentHomeBinding
import com.drdisagree.colorblendr.service.AutoStartService.Companion.isServiceNotRunning
import com.drdisagree.colorblendr.service.RestartBroadcastReceiver.Companion.scheduleJob
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.app.AppUtil.hasStoragePermission
import com.drdisagree.colorblendr.utils.app.AppUtil.openAppSettings
import com.drdisagree.colorblendr.utils.app.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.app.AppUtil.requestStoragePermission
import com.drdisagree.colorblendr.utils.app.FragmentUtil.TabSelection
import com.drdisagree.colorblendr.utils.app.FragmentUtil.getSlidingDirection
import com.drdisagree.colorblendr.utils.app.FragmentUtil.setCustomAnimations
import com.drdisagree.colorblendr.utils.app.parcelable
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        myFragmentManager = getChildFragmentManager()

        if (savedInstanceState == null) {
            replaceFragment(ColorsFragment())
        }

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigationView()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (permissionsGranted(requireContext())) {
                    if (isServiceNotRunning &&
                        arguments?.getBoolean("success", false) == true
                    ) {
                        scheduleJob(requireContext())
                    }
                } else {
                    requestPermissionsLauncher.launch(AppUtil.REQUIRED_PERMISSIONS)
                }
            } catch (ignored: Exception) {
            }
        }, 2000)

        arguments?.let { bundle ->
            bundle.parcelable<Uri>("data")?.let { uri ->
                replaceFragment(
                    SettingsFragment().apply {
                        arguments = Bundle().apply {
                            putParcelable("data", uri)
                        }
                    }
                )
                bundle.remove("data")
            }
        }

        registerOnBackPressedCallback()
    }

    private fun setupBottomNavigationView() {
        getChildFragmentManager().addOnBackStackChangedListener {
            val tag: String? = topFragment

            when (tag) {
                ColorsFragment::class.java.simpleName -> {
                    binding.bottomNavigationView.menu.getItem(0).setChecked(true)
                }

                ThemeFragment::class.java.simpleName -> {
                    binding.bottomNavigationView.menu.getItem(1).setChecked(true)
                }

                StylesFragment::class.java.simpleName -> {
                    binding.bottomNavigationView.menu.getItem(2).setChecked(true)
                }

                SettingsFragment::class.java.simpleName -> {
                    binding.bottomNavigationView.menu.getItem(3).setChecked(true)
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_colors -> {
                    replaceFragment(ColorsFragment())
                }

                R.id.nav_themes -> {
                    replaceFragment(ThemeFragment())
                }

                R.id.nav_styles -> {
                    replaceFragment(StylesFragment())
                }

                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
            true
        }

        binding.bottomNavigationView.setOnItemReselectedListener { }
    }

    private fun registerOnBackPressedCallback() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragmentManager: FragmentManager = getChildFragmentManager()

                    if (fragmentManager.backStackEntryCount > 0) {
                        fragmentManager.popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }
            })
    }

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            this.handlePermissionsResult(
                result
            )
        }

    private fun handlePermissionsResult(result: Map<String, Boolean>) {
        for (pair: Map.Entry<String, Boolean> in result.entries) {
            val permission: String = pair.key
            val granted: Boolean = pair.value

            if (!granted) {
                showGeneralPermissionSnackbar(
                    permission,
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        permission
                    )
                )
                return
            }
        }

        if (!hasStoragePermission()) {
            showStoragePermissionSnackbar()
            return
        }

        if (isServiceNotRunning &&
            arguments?.getBoolean("success", false) == true
        ) {
            scheduleJob(requireContext())
        }
    }

    private fun showGeneralPermissionSnackbar(permission: String, permanentlyDenied: Boolean) {
        val snackbar: Snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.permission_must_be_granted,
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction(R.string.grant) {
            if (!permanentlyDenied) {
                requestPermissionsLauncher.launch(arrayOf(permission))
            } else {
                openAppSettings(requireContext())
            }
            snackbar.dismiss()
        }

        snackbar.show()
    }

    private fun showStoragePermissionSnackbar() {
        val snackbar: Snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            R.string.file_access_permission_required,
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setAction(R.string.grant) {
            requestStoragePermission(requireContext())
            snackbar.dismiss()
        }

        snackbar.show()
    }

    private val topFragment: String?
        get() {
            val fragment: Array<String?> = arrayOf(null)

            val fragmentManager: FragmentManager = getChildFragmentManager()
            val last: Int = fragmentManager.fragments.size - 1

            if (last >= 0) {
                val topFragment: Fragment = fragmentManager.fragments[last]
                currentFragment = topFragment

                when (topFragment) {
                    is ColorsFragment -> fragment[0] = ColorsFragment::class.java.simpleName

                    is ThemeFragment -> fragment[0] = ThemeFragment::class.java.simpleName

                    is StylesFragment -> fragment[0] = StylesFragment::class.java.simpleName

                    is SettingsFragment -> fragment[0] = SettingsFragment::class.java.simpleName
                }
            }

            return fragment[0]
        }

    companion object {
        private var currentFragment: Fragment? = null
        private lateinit var myFragmentManager: FragmentManager

        fun replaceFragment(fragment: Fragment) {
            val tag: String = fragment.javaClass.simpleName
            val fragmentTransaction: FragmentTransaction = myFragmentManager.beginTransaction()

            val direction: TabSelection = getSlidingDirection(currentFragment, fragment)
            if (currentFragment != null) {
                setCustomAnimations(direction, fragmentTransaction)
            }

            fragmentTransaction.replace(
                R.id.fragmentContainer,
                fragment
            )

            when (tag) {
                ColorsFragment::class.java.simpleName -> {
                    myFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }

                ThemeFragment::class.java.simpleName, StylesFragment::class.java.simpleName, SettingsFragment::class.java.simpleName -> {
                    myFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    fragmentTransaction.addToBackStack(tag)
                }

                else -> {
                    fragmentTransaction.addToBackStack(tag)
                }
            }

            fragmentTransaction.commit()
            currentFragment = fragment
        }
    }
}