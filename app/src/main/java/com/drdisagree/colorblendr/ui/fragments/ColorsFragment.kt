package com.drdisagree.colorblendr.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Utilities.clearAllOverriddenColors
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.databinding.FragmentColorsBinding
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.ui.views.WallColorPreview
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.dialogs.ColorPickerDialog
import com.drdisagree.colorblendr.ui.widgets.colorpickerdialog.views.picker.ImagePickerView

@Suppress("deprecation")
class ColorsFragment : BaseFragment() {

    private lateinit var binding: FragmentColorsBinding
    private lateinit var monetSeedColor: IntArray
    private val colorsViewModel: ColorsViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val wallpaperChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            colorsViewModel.loadWallpaperColors()
        }
    }

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
        binding = FragmentColorsBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.app_name, false, binding.header.toolbar)

        monetSeedColor = intArrayOf(getSeedColorValue(0))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.getVisibilityStates()
            .observe(getViewLifecycleOwner()) { visibilityStates: Map<String, Int> ->
                this.updateViewVisibility(visibilityStates)
            }

        // Inflate color containers
        colorsViewModel.wallpaperColorPalettes.observe(viewLifecycleOwner) { colorPalettes ->
            addColorsToContainer(colorPalettes, true)
            updateColorContainers()
        }
        colorsViewModel.basicColorPalettes.observe(viewLifecycleOwner) { colorPalettes ->
            addColorsToContainer(colorPalettes, false)
        }

        // Color picker
        binding.seedColorPicker.previewColor = getSeedColorValue(monetSeedColor[0])

        binding.seedColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(24f)
                .withColor(monetSeedColor[0])
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView::class.java)
                .withListener { _: ColorPickerDialog?, color: Int ->
                    if (monetSeedColor[0] != color) {
                        monetSeedColor[0] = color
                        binding.seedColorPicker.previewColor = color
                        setSeedColorValue(monetSeedColor[0])

                        CoroutineScope(Dispatchers.Main).launch {
                            updateColorAppliedTimestamp()
                            delay(300)
                            withContext(Dispatchers.IO) {
                                applyFabricatedColors()
                            }
                            colorsViewModel.refreshData()
                        }
                    }
                }
                .show(getChildFragmentManager(), "seedColorPicker")
        }
        binding.seedColorPicker.visibility = if (customColorEnabled()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Color palette
        binding.colorPalette.setOnClickListener {
            HomeFragment.replaceFragment(ColorPaletteFragment())
        }

        // Force per app theme
        binding.perAppTheme.setOnClickListener {
            HomeFragment.replaceFragment(PerAppThemeFragment())
        }
        binding.perAppTheme.setEnabled(isRootMode())
    }

    private fun updateColorContainers() {
        val wallpaperColorSelected = !customColorEnabled()
                && colorsViewModel.wallpaperColors.value.orEmpty().contains(getSeedColorValue())
        binding.colorsToggleGroup.check(
            if (wallpaperColorSelected) R.id.wallpaper_colors_button else R.id.basic_colors_button
        )
        binding.colorsToggleGroup.addOnButtonCheckedListener { _: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                if (checkedId == R.id.wallpaper_colors_button) {
                    binding.basicColorsContainer.visibility = View.GONE
                    binding.wallpaperColorsContainer.visibility = View.VISIBLE
                } else {
                    binding.wallpaperColorsContainer.visibility = View.GONE
                    binding.basicColorsContainer.visibility = View.VISIBLE
                }
            }
        }

        if (wallpaperColorSelected) {
            binding.basicColorsContainer.visibility = View.GONE
            binding.wallpaperColorsContainer.visibility = View.VISIBLE
        } else {
            binding.wallpaperColorsContainer.visibility = View.GONE
            binding.basicColorsContainer.visibility = View.VISIBLE
        }
    }

    private fun updateViewVisibility(visibilityStates: Map<String, Int>) {
        val seedColorVisibility: Int? = visibilityStates[MONET_SEED_COLOR_ENABLED]

        if (seedColorVisibility != null && binding.seedColorPicker.visibility != seedColorVisibility) {
            binding.seedColorPicker.visibility = seedColorVisibility

            val wallpaperColorList = getWallpaperColorList()
            val wallpaperColor = if (wallpaperColorList.isNotEmpty()) wallpaperColorList[0]
            else Color.BLUE

            if (seedColorVisibility == View.GONE) {
                monetSeedColor = intArrayOf(wallpaperColor)
                binding.seedColorPicker.previewColor = monetSeedColor[0]
            } else {
                monetSeedColor = intArrayOf(getSeedColorValue(wallpaperColor))
                binding.seedColorPicker.previewColor = monetSeedColor[0]
            }
        }
    }

    private fun addColorsToContainer(
        colorPalettes: Map<Int, List<List<Int>>>,
        isWallpaperColors: Boolean
    ) {
        if (isWallpaperColors) {
            binding.wallpaperColorsContainer
        } else {
            binding.basicColorsContainer
        }.removeAllViews()

        val seedColor = getSeedColorValue()
        val customColorEnabled = customColorEnabled()

        if (isWallpaperColors) {
            colorsViewModel.wallpaperColors.value.orEmpty()
        } else {
            colorsViewModel.basicColors.value.orEmpty()
        }.forEach { color ->
            val size: Int = (48 * resources.displayMetrics.density).toInt()
            val margin: Int = (12 * resources.displayMetrics.density).toInt()

            val colorPreview = WallColorPreview(requireContext()).apply {
                setLayoutParams(
                    LinearLayout.LayoutParams(size, size).apply {
                        setMargins(margin, margin, margin, margin)
                    }
                )
                setPreviewColors(
                    color = color,
                    colorPaletteList = colorPalettes[color],
                )
                tag = color
                isSelected = color == seedColor &&
                        if (isWallpaperColors) !customColorEnabled
                        else true

                setOnClickListener {
                    if (!isSelected) {
                        resetCustomStyleIfNotNull()
                    }

                    setSeedColorValue(tag as Int)
                    setCustomColorEnabled(!isWallpaperColors)
                    binding.seedColorPicker.previewColor = tag as Int

                    updateColorPreviewSelection(this)

                    CoroutineScope(Dispatchers.Main).launch {
                        updateColorAppliedTimestamp()
                        withContext(Dispatchers.IO) {
                            try {
                                applyFabricatedColors()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }

            if (isWallpaperColors) {
                binding.wallpaperColorsContainer
            } else {
                binding.basicColorsContainer
            }.addView(colorPreview)
        }
    }

    private fun updateColorPreviewSelection(selectedColorPreview: WallColorPreview) {
        for (container in listOf(binding.wallpaperColorsContainer, binding.basicColorsContainer)) {
            val childCount = container.childCount
            for (i in 0 until childCount) {
                val child = container.getChildAt(i) as WallColorPreview
                child.isSelected = child == selectedColorPreview
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED)

        LocalBroadcastManager
            .getInstance(requireContext())
            .registerReceiver(wallpaperChangedReceiver, intentFilter)
    }

    override fun onDestroy() {
        try {
            LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(wallpaperChangedReceiver)
        } catch (_: Exception) {
            // Receiver was not registered
        }
        super.onDestroy()
    }
}