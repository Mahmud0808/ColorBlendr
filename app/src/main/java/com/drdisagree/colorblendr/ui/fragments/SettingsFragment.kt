package com.drdisagree.colorblendr.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.drdisagree.colorblendr.ui.compose.screens.settings.SettingsScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.app.parcelable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var restoreUri by androidx.compose.runtime.remember {
                    mutableStateOf(arguments?.parcelable<Uri>("data"))
                }

                ColorBlendrTheme {
                    SettingsScreen(
                        sharedViewModel = sharedViewModel,
                        restoreUri = restoreUri,
                        onRestoreUriConsumed = {
                            restoreUri = null
                            arguments?.remove("data")
                        },
                        onNavigateToAbout = {
                            HomeFragment.replaceFragment(AboutFragment())
                        },
                        onNavigateToAdvanced = {
                            HomeFragment.replaceFragment(SettingsAdvancedFragment())
                        },
                        onNavigateToPrivacyPolicy = {
                            HomeFragment.replaceFragment(PrivacyPolicyFragment())
                        }
                    )
                }
            }
        }
    }
}
