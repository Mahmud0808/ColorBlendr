package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.ui.compose.screens.perapp.PerAppThemeScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

class PerAppThemeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ColorBlendrTheme {
                    PerAppThemeScreen()
                }
            }
        }
    }
}
