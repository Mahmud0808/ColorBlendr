package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.drdisagree.colorblendr.data.common.Utilities.clearAllOverriddenColors
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.ui.compose.screens.colors.ColorsScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ColorsFragment : BaseFragment() {

    private val colorsViewModel: ColorsViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isShizukuMode()) {
            clearAllOverriddenColors()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ColorBlendrTheme {
                    ColorsScreen(
                        colorsViewModel = colorsViewModel,
                        sharedViewModel = sharedViewModel,
                        fragmentManager = childFragmentManager,
                        onNavigateToColorPalette = {
                            HomeFragment.replaceFragment(ColorPaletteFragment())
                        },
                        onNavigateToPerAppTheme = {
                            HomeFragment.replaceFragment(PerAppThemeFragment())
                        }
                    )
                }
            }
        }
    }
}
