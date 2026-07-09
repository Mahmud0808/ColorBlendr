package com.drdisagree.colorblendr.ui.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Utilities.setFirstRunCompleted
import com.drdisagree.colorblendr.data.common.Utilities.setWorkingMethod
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.ui.activities.MainActivity
import com.drdisagree.colorblendr.ui.compose.screens.onboarding.OnboardingScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.fragments.HomeFragment
import com.drdisagree.colorblendr.ui.fragments.PairingFragment
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.requestShizukuPermission
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperColorUtil.updateWallpaperColorList
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ColorBlendrTheme {
                    OnboardingScreen(
                        onCheckRootConnection = ::checkRootConnection,
                        onCheckShizukuConnection = ::checkShizukuConnection,
                        onCheckAdbConnection = ::checkAdbConnection,
                        onNavigateToPairing = {
                            MainActivity.replaceFragment(PairingFragment(), true)
                        },
                        popActivityBackStack = {
                            val fragmentManager = requireActivity().supportFragmentManager
                            if (fragmentManager.backStackEntryCount > 0) {
                                fragmentManager.popBackStack()
                                true
                            } else {
                                false
                            }
                        },
                        onFinishActivity = { requireActivity().finish() }
                    )
                }
            }
        }
    }

    private fun checkRootConnection() {
        RootConnectionProvider
            .builder(requireContext())
            .onSuccess { goToHomeFragment() }
            .run()
    }

    private fun checkShizukuConnection() {
        if (isShizukuAvailable) {
            requestShizukuPermission(requireActivity()) { granted: Boolean ->
                if (granted) {
                    bindUserService(
                        getUserServiceArgs(ShizukuConnection::class.java),
                        ShizukuConnectionProvider.serviceConnection
                    )
                    goToHomeFragment()
                } else {
                    Toast.makeText(
                        appContext,
                        R.string.shizuku_service_not_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                appContext,
                R.string.shizuku_service_not_found,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkAdbConnection() {
        if (WifiAdbShell.isMyDeviceConnected()) {
            goToHomeFragment()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.wireless_adb_not_connected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun goToHomeFragment() {
        lifecycleScope.launch {
            try {
                updateWallpaperColorList(requireContext())
                updateFabricatedAppList(requireContext())

                setFirstRunCompleted()
                setWorkingMethod(WORKING_METHOD)

                MainActivity.replaceFragment(
                    HomeFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("success", true)
                        }
                    },
                    true
                )
            } catch (_: Exception) {
            }
        }
    }
}
