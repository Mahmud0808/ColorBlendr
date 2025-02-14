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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.ui.views.WallColorPreview
import com.drdisagree.colorblendr.utils.ColorUtil.monetAccentColors
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView
import java.util.Arrays
import java.util.stream.Collectors

@Suppress("deprecation")
class ColorsFragment : Fragment() {

    private lateinit var binding: FragmentColorsBinding
    private lateinit var monetSeedColor: IntArray
    private lateinit var sharedViewModel: SharedViewModel

    private val wallpaperChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            addWallpaperColorItems()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

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
                this.updateViewVisibility(
                    visibilityStates
                )
            }

        // Color codes
        val wallpaperColorSelected =
            !customColorEnabled() && getWallpaperColors().contains(getSeedColorValue())
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

        // Inflate color containers
        addWallpaperColorItems()
        addBasicColorItems()

        if (wallpaperColorSelected) {
            binding.basicColorsContainer.visibility = View.GONE
            binding.wallpaperColorsContainer.visibility = View.VISIBLE
        } else {
            binding.wallpaperColorsContainer.visibility = View.GONE
            binding.basicColorsContainer.visibility = View.VISIBLE
        }

        // Primary color
        binding.seedColorPicker.previewColor = getSeedColorValue(monetSeedColor[0])

        binding.seedColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(10f)
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
                                try {
                                    applyFabricatedColors()
                                } catch (ignored: Exception) {
                                }
                            }
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
            HomeFragment.replaceFragment(
                ColorPaletteFragment()
            )
        }

        // Force per app theme
        binding.perAppTheme.setOnClickListener {
            HomeFragment.replaceFragment(
                PerAppThemeFragment()
            )
        }
        binding.perAppTheme.setEnabled(isRootMode())
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

    private fun addWallpaperColorItems() {
        val wallpaperColorList: ArrayList<Int> = getWallpaperColors()

        addColorsToContainer(wallpaperColorList, true)
    }

    private fun addBasicColorItems() {
        val basicColors: Array<String> = resources.getStringArray(R.array.basic_color_codes)
        val basicColorList: List<Int> = Arrays.stream(basicColors)
            .map { colorString: String? -> Color.parseColor(colorString) }
            .collect(Collectors.toList())

        addColorsToContainer(ArrayList(basicColorList), false)
    }

    private fun addColorsToContainer(colorList: ArrayList<Int>, isWallpaperColors: Boolean) {
        if (isWallpaperColors) {
            binding.wallpaperColorsContainer
        } else {
            binding.basicColorsContainer
        }.removeAllViews()

        for (i in colorList.indices) {
            val size: Int = (48 * resources.displayMetrics.density).toInt()
            val margin: Int = (12 * resources.displayMetrics.density).toInt()

            val colorPreview = WallColorPreview(requireContext()).apply {
                setLayoutParams(
                    LinearLayout.LayoutParams(size, size).apply {
                        setMargins(margin, margin, margin, margin)
                    }
                )
                setMainColor(colorList[i])
                tag = colorList[i]
                isSelected = colorList[i] == getSeedColorValue() &&
                        if (isWallpaperColors) !customColorEnabled()
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
                            } catch (ignored: Exception) {
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

    private fun getWallpaperColors(): ArrayList<Int> {
        return getWallpaperColorList().ifEmpty { monetAccentColors }
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
        } catch (ignored: Exception) {
            // Receiver was not registered
        }
        super.onDestroy()
    }
}