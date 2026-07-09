package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.drdisagree.colorblendr.ui.compose.screens.styles.StylesScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StylesFragment : BaseFragment() {

    private val stylesViewModel: StylesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ColorBlendrTheme {
                    StylesScreen(stylesViewModel = stylesViewModel)
                }
            }
        }
    }
}
