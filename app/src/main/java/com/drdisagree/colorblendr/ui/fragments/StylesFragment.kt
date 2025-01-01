package com.drdisagree.colorblendr.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.MONET_STYLE
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding
import com.drdisagree.colorblendr.ui.adapters.StylePreviewAdapter
import com.drdisagree.colorblendr.utils.ColorSchemeUtil
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.MONET.Companion.toEnumMonet
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle

class StylesFragment : Fragment() {

    private lateinit var binding: FragmentStylesBinding
    private val notShizukuMode = workingMethod != Const.WorkMethod.SHIZUKU
    private val isAtleastA13 = notShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private val isAtleastA14 = notShizukuMode ||
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStylesBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.styles, true, binding.header.toolbar)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = StylePreviewAdapter(getStyleList())

        return binding.root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            parentFragmentManager.popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStyleList(): List<StylePreviewAdapter.StyleData> {
        val selectedStyle = RPrefs.getString(MONET_STYLE, null)?.toEnumMonet()

        return listOf(
            StylePreviewAdapter.StyleData(
                R.string.monet_neutral,
                R.string.monet_neutral_desc,
                isEnabled = isAtleastA13,
                isSelected = ColorSchemeUtil.MONET.SPRITZ == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_monochrome,
                R.string.monet_monochrome_desc,
                isEnabled = isAtleastA14,
                isSelected = ColorSchemeUtil.MONET.MONOCHROMATIC == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_tonalspot,
                R.string.monet_tonalspot_desc,
                isEnabled = true,
                isSelected = ColorSchemeUtil.MONET.TONAL_SPOT == selectedStyle || selectedStyle == null
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_vibrant,
                R.string.monet_vibrant_desc,
                isEnabled = isAtleastA13,
                isSelected = ColorSchemeUtil.MONET.VIBRANT == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_rainbow,
                R.string.monet_rainbow_desc,
                isEnabled = isAtleastA13,
                isSelected = ColorSchemeUtil.MONET.RAINBOW == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_expressive,
                R.string.monet_expressive_desc,
                isEnabled = isAtleastA13,
                isSelected = ColorSchemeUtil.MONET.EXPRESSIVE == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_fidelity,
                R.string.monet_fidelity_desc,
                isEnabled = notShizukuMode,
                isSelected = ColorSchemeUtil.MONET.FIDELITY == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_content,
                R.string.monet_content_desc,
                isEnabled = notShizukuMode,
                isSelected = ColorSchemeUtil.MONET.CONTENT == selectedStyle
            ),
            StylePreviewAdapter.StyleData(
                R.string.monet_fruitsalad,
                R.string.monet_fruitsalad_desc,
                isEnabled = isAtleastA13,
                isSelected = ColorSchemeUtil.MONET.FRUIT_SALAD == selectedStyle
            )
        )
    }
}