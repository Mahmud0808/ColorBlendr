package com.drdisagree.colorblendr.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.getBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.getCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.resetBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setAccentSaturation
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundLightness
import com.drdisagree.colorblendr.data.common.Utilities.setBackgroundSaturation
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.SystemUtil.isDarkMode
import com.drdisagree.colorblendr.utils.colors.ColorUtil
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemeFragment : BaseFragment() {

    private lateinit var binding: FragmentThemeBinding
    private var monetAccentSaturation = getAccentSaturation()
    private var monetBackgroundSaturation = getBackgroundSaturation()
    private var monetBackgroundLightness = getBackgroundLightness()
    private var wasDarkMode: Boolean = isDarkMode

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.theme, true, binding.header.toolbar)

        // Color preview titles
        binding.apply {
            colorAccent1.title.setText(R.string.primary)
            colorAccent2.title.setText(R.string.secondary)
            colorAccent3.title.setText(R.string.tertiary)
            colorNeutral1.title.setText(R.string.neutral_1)
            colorNeutral2.title.setText(R.string.neutral_2)

            // Monet primary accent saturation
            accentSaturation.seekbarProgress = monetAccentSaturation
            accentSaturation.setOnSeekbarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    accentSaturation.setSelectedProgress()
                    monetAccentSaturation = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetAccentSaturation = seekBar.progress
                    setAccentSaturation(monetAccentSaturation)
                    updateColors()
                }
            })

            // Long Click Reset
            accentSaturation.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetAccentSaturation = 100
                updatePreviewColors()
                resetAccentSaturation()
                updateColors()
                true
            }
            accentSaturation.setEnabled(isRootMode())

            // Monet background saturation
            backgroundSaturation.seekbarProgress = monetBackgroundSaturation
            backgroundSaturation.setOnSeekbarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    backgroundSaturation.setSelectedProgress()
                    monetBackgroundSaturation = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetBackgroundSaturation = seekBar.progress
                    setBackgroundSaturation(monetBackgroundSaturation)
                    updateColors()
                }
            })

            // Reset button
            backgroundSaturation.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetBackgroundSaturation = 100
                updatePreviewColors()
                resetBackgroundSaturation()
                updateColors()
                true
            }
            backgroundSaturation.setEnabled(isRootMode())

            // Monet background lightness
            backgroundLightness.seekbarProgress = monetBackgroundLightness
            backgroundLightness.setOnSeekbarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    backgroundLightness.setSelectedProgress()
                    monetBackgroundLightness = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetBackgroundLightness = seekBar.progress
                    setBackgroundLightness(monetBackgroundLightness)
                    updateColors()
                }
            })

            // Long Click Reset
            backgroundLightness.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetBackgroundLightness = 100
                updatePreviewColors()
                resetBackgroundLightness()
                updateColors()
                true
            }
            backgroundLightness.setEnabled(isRootMode())
        }

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updatePreviewColors()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val isDarkMode =
            newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode != wasDarkMode) {
            updateProgressBars()
            wasDarkMode = isDarkMode
        }
    }

    private fun updateProgressBars() {
        binding.apply {
            monetAccentSaturation = getAccentSaturation()
            monetBackgroundSaturation = getBackgroundSaturation()
            monetBackgroundLightness = getBackgroundLightness()

            accentSaturation.post {
                accentSaturation.seekbarProgress = monetAccentSaturation
            }
            backgroundSaturation.post {
                backgroundSaturation.seekbarProgress = monetBackgroundSaturation
            }
            backgroundLightness.post {
                backgroundLightness.seekbarProgress = monetBackgroundLightness
            }
        }
    }

    private fun updatePreviewColors() {
        val colorPalette: ArrayList<ArrayList<Int>> = generateModifiedColors(
            getCurrentMonetStyle(),
            monetAccentSaturation,
            monetBackgroundSaturation,
            monetBackgroundLightness,
            pitchBlackThemeEnabled(),
            accurateShadesEnabled()
        )

        val isDarkMode: Boolean = isDarkMode

        binding.colorAccent1.colorContainer.apply {
            setHalfCircleColor(colorPalette[0][4])
            setFirstQuarterCircleColor(colorPalette[0][5])
            setSecondQuarterCircleColor(colorPalette[0][6])
            setSquareColor(colorPalette[0][if (!isDarkMode) 3 else 9])
            setPadding(8f)
            invalidateColors()
        }

        binding.colorAccent2.colorContainer.apply {
            setHalfCircleColor(colorPalette[1][4])
            setFirstQuarterCircleColor(colorPalette[1][5])
            setSecondQuarterCircleColor(colorPalette[1][6])
            setSquareColor(colorPalette[1][if (!isDarkMode) 3 else 9])
            setPadding(8f)
            invalidateColors()
        }

        binding.colorAccent3.colorContainer.apply {
            setHalfCircleColor(colorPalette[2][4])
            setFirstQuarterCircleColor(colorPalette[2][5])
            setSecondQuarterCircleColor(colorPalette[2][6])
            setSquareColor(colorPalette[2][if (!isDarkMode) 3 else 9])
            setPadding(8f)
            invalidateColors()
        }

        binding.colorNeutral1.colorContainer.apply {
            setHalfCircleColor(colorPalette[3][4])
            setFirstQuarterCircleColor(colorPalette[3][5])
            setSecondQuarterCircleColor(colorPalette[3][6])
            setSquareColor(colorPalette[3][if (!isDarkMode) 3 else 9])
            setPadding(8f)
            invalidateColors()
        }

        binding.colorNeutral2.colorContainer.apply {
            setHalfCircleColor(colorPalette[4][4])
            setFirstQuarterCircleColor(colorPalette[4][5])
            setSecondQuarterCircleColor(colorPalette[4][6])
            setSquareColor(colorPalette[4][if (!isDarkMode) 3 else 9])
            setPadding(8f)
            invalidateColors()
        }
    }

    private fun generateModifiedColors(
        style: MONET,
        monetAccentSaturation: Int,
        monetBackgroundSaturation: Int,
        monetBackgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean
    ): ArrayList<ArrayList<Int>> {
        return ColorUtil.generateModifiedColors(
            style,
            monetAccentSaturation,
            monetBackgroundSaturation,
            monetBackgroundLightness,
            pitchBlackTheme,
            accurateShades
        )
    }

    private fun updateColors() {
        CoroutineScope(Dispatchers.Main).launch {
            updateColorAppliedTimestamp()
            delay(200)
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
        }
    }
}