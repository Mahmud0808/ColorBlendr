package com.drdisagree.colorblendr.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_STYLE
import com.drdisagree.colorblendr.common.Const.MONET_STYLE_ORIGINAL_NAME
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.config.RPrefs.putString
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding
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

        val selectedStyle = RPrefs.getString(MONET_STYLE, null)

        binding.monetNeutral.isSelected = getString(R.string.monet_neutral) == selectedStyle
        binding.monetNeutral.setOnClickListener {
            binding.monetNeutral.isSelected = true
            unSelectOthers(binding.monetNeutral)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_neutral))
            binding.monetNeutral.applyColorScheme()
        }
        binding.monetNeutral.isEnabled = isAtleastA13

        binding.monetMonochrome.isSelected = getString(R.string.monet_monochrome) == selectedStyle
        binding.monetMonochrome.setOnClickListener {
            binding.monetMonochrome.isSelected = true
            unSelectOthers(binding.monetMonochrome)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_monochrome))
            binding.monetMonochrome.applyColorScheme()
        }
        binding.monetMonochrome.isEnabled = isAtleastA14

        binding.monetTonalspot.isSelected =
            getString(R.string.monet_tonalspot) == selectedStyle || selectedStyle == null
        binding.monetTonalspot.setOnClickListener {
            binding.monetTonalspot.isSelected = true
            unSelectOthers(binding.monetTonalspot)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_tonalspot))
            binding.monetTonalspot.applyColorScheme()
        }

        binding.monetVibrant.isSelected = getString(R.string.monet_vibrant) == selectedStyle
        binding.monetVibrant.setOnClickListener {
            binding.monetVibrant.isSelected = true
            unSelectOthers(binding.monetVibrant)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_vibrant))
            binding.monetVibrant.applyColorScheme()
        }
        binding.monetVibrant.isEnabled = isAtleastA13

        binding.monetRainbow.isSelected = getString(R.string.monet_rainbow) == selectedStyle
        binding.monetRainbow.setOnClickListener {
            binding.monetRainbow.isSelected = true
            unSelectOthers(binding.monetRainbow)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_rainbow))
            binding.monetRainbow.applyColorScheme()
        }
        binding.monetRainbow.isEnabled = isAtleastA13

        binding.monetExpressive.isSelected = getString(R.string.monet_expressive) == selectedStyle
        binding.monetExpressive.setOnClickListener {
            binding.monetExpressive.isSelected = true
            unSelectOthers(binding.monetExpressive)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_expressive))
            binding.monetExpressive.applyColorScheme()
        }
        binding.monetExpressive.isEnabled = isAtleastA13

        binding.monetFidelity.isSelected = getString(R.string.monet_fidelity) == selectedStyle
        binding.monetFidelity.setOnClickListener {
            binding.monetFidelity.isSelected = true
            unSelectOthers(binding.monetFidelity)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_fidelity))
            binding.monetFidelity.applyColorScheme()
        }
        binding.monetFidelity.isEnabled = notShizukuMode

        binding.monetContent.isSelected = getString(R.string.monet_content) == selectedStyle
        binding.monetContent.setOnClickListener {
            binding.monetContent.isSelected = true
            unSelectOthers(binding.monetContent)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_content))
            binding.monetContent.applyColorScheme()
        }
        binding.monetContent.isEnabled = notShizukuMode

        binding.monetFruitsalad.isSelected = getString(R.string.monet_fruitsalad) == selectedStyle
        binding.monetFruitsalad.setOnClickListener {
            binding.monetFruitsalad.isSelected = true
            unSelectOthers(binding.monetFruitsalad)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            putString(MONET_STYLE_ORIGINAL_NAME, getOriginalName(R.string.monet_fruitsalad))
            binding.monetFruitsalad.applyColorScheme()
        }
        binding.monetFruitsalad.isEnabled = isAtleastA13

        return binding.root
    }

    private fun unSelectOthers(viewGroup: ViewGroup) {
        val viewGroups = arrayOf<ViewGroup>(
            binding.monetNeutral,
            binding.monetMonochrome,
            binding.monetTonalspot,
            binding.monetVibrant,
            binding.monetRainbow,
            binding.monetExpressive,
            binding.monetFidelity,
            binding.monetContent,
            binding.monetFruitsalad
        )

        for (view in viewGroups) {
            if (view !== viewGroup) {
                view.isSelected = false
            }
        }
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

    private fun getOriginalName(@StringRes id: Int): String {
        val name = getString(id)

        return when (name) {
            getString(R.string.monet_neutral) -> {
                "SPRITZ"
            }

            getString(R.string.monet_vibrant) -> {
                "VIBRANT"
            }

            getString(R.string.monet_expressive) -> {
                "EXPRESSIVE"
            }

            getString(R.string.monet_rainbow) -> {
                "RAINBOW"
            }

            getString(R.string.monet_fruitsalad) -> {
                "FRUIT_SALAD"
            }

            getString(R.string.monet_content) -> {
                "CONTENT"
            }

            getString(R.string.monet_monochrome) -> {
                "MONOCHROMATIC"
            }

            getString(R.string.monet_fidelity) -> {
                "FIDELITY"
            }

            else -> {
                "TONAL_SPOT"
            }
        }
    }
}