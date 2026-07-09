package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.drdisagree.colorblendr.ui.compose.screens.palette.ColorPaletteScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ColorPaletteFragment : BaseFragment() {

    private val colorPaletteViewModel: ColorPaletteViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ColorBlendrTheme {
                    ColorPaletteScreen(
                        colorPaletteViewModel = colorPaletteViewModel,
                        sharedViewModel = sharedViewModel,
                        fragmentManager = childFragmentManager
                    )
                }
            }
        }
    }
}
