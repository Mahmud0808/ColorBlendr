package com.drdisagree.colorblendr.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.MONET_ACCENT_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_LIGHTNESS
import com.drdisagree.colorblendr.common.Const.MONET_BACKGROUND_SATURATION
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs.clearPref
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.getCurrentMonetStyle
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.utils.ColorUtil
import com.drdisagree.colorblendr.utils.MONET
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.SystemUtil.isDarkMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemeFragment : Fragment() {

    private lateinit var binding: FragmentThemeBinding
    private val monetAccentSaturation: IntArray = intArrayOf(
        getInt(MONET_ACCENT_SATURATION, 100)
    )
    private val monetBackgroundSaturation: IntArray = intArrayOf(
        getInt(MONET_BACKGROUND_SATURATION, 100)
    )
    private val monetBackgroundLightness: IntArray = intArrayOf(
        getInt(MONET_BACKGROUND_LIGHTNESS, 100)
    )
    private var wasDarkMode: Boolean = isDarkMode
    private val notShizukuMode: Boolean = workingMethod != Const.WorkMethod.SHIZUKU

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
            accentSaturation.seekbarProgress = getInt(MONET_ACCENT_SATURATION, 100)
            accentSaturation.setOnSeekbarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    accentSaturation.setSelectedProgress()
                    monetAccentSaturation[0] = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetAccentSaturation[0] = seekBar.progress
                    putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0])
                    applyFabricatedColors()
                }
            })

            // Long Click Reset
            accentSaturation.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetAccentSaturation[0] = 100
                updatePreviewColors()
                clearPref(MONET_ACCENT_SATURATION)
                applyFabricatedColors()
                true
            }
            accentSaturation.setEnabled(notShizukuMode)

            // Monet background saturation
            backgroundSaturation.seekbarProgress = getInt(MONET_BACKGROUND_SATURATION, 100)
            backgroundSaturation.setOnSeekbarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    backgroundSaturation.setSelectedProgress()
                    monetBackgroundSaturation[0] = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetBackgroundSaturation[0] = seekBar.progress
                    putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0])
                    applyFabricatedColors()
                }
            })

            // Reset button
            backgroundSaturation.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetBackgroundSaturation[0] = 100
                updatePreviewColors()
                clearPref(MONET_BACKGROUND_SATURATION)
                applyFabricatedColors()
                true
            }
            backgroundSaturation.setEnabled(notShizukuMode)

            // Monet background lightness
            backgroundLightness.seekbarProgress = getInt(MONET_BACKGROUND_LIGHTNESS, 100)
            backgroundLightness.setOnSeekbarChangeListener(object :
                OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    backgroundLightness.setSelectedProgress()
                    monetBackgroundLightness[0] = progress
                    updatePreviewColors()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    resetCustomStyleIfNotNull()
                    monetBackgroundLightness[0] = seekBar.progress
                    putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0])
                    applyFabricatedColors()
                }
            })

            // Long Click Reset
            backgroundLightness.setResetClickListener {
                resetCustomStyleIfNotNull()
                monetBackgroundLightness[0] = 100
                updatePreviewColors()
                clearPref(MONET_BACKGROUND_LIGHTNESS)
                applyFabricatedColors()
                true
            }
            backgroundLightness.setEnabled(notShizukuMode)
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
            monetAccentSaturation[0] = getInt(MONET_ACCENT_SATURATION, 100)
            monetBackgroundSaturation[0] = getInt(MONET_BACKGROUND_SATURATION, 100)
            monetBackgroundLightness[0] = getInt(MONET_BACKGROUND_LIGHTNESS, 100)

            accentSaturation.post {
                accentSaturation.seekbarProgress = monetAccentSaturation[0]
            }
            backgroundSaturation.post {
                backgroundSaturation.seekbarProgress = monetBackgroundSaturation[0]
            }
            backgroundLightness.post {
                backgroundLightness.seekbarProgress = monetBackgroundLightness[0]
            }
        }
    }

    private fun applyFabricatedColors() {
        CoroutineScope(Dispatchers.Main).launch {
            putLong(
                MONET_LAST_UPDATED,
                System.currentTimeMillis()
            )
            delay(200)
            withContext(Dispatchers.IO) {
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun updatePreviewColors() {
        val colorPalette: ArrayList<ArrayList<Int>>? = generateModifiedColors(
            getCurrentMonetStyle(),
            monetAccentSaturation[0],
            monetBackgroundSaturation[0],
            monetBackgroundLightness[0],
            getBoolean(MONET_PITCH_BLACK_THEME, false),
            getBoolean(MONET_ACCURATE_SHADES, true)
        )

        if (colorPalette != null) {
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
    }

    private fun generateModifiedColors(
        style: MONET,
        monetAccentSaturation: Int,
        monetBackgroundSaturation: Int,
        monetBackgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean
    ): ArrayList<ArrayList<Int>>? {
        try {
            return ColorUtil.generateModifiedColors(
                style,
                monetAccentSaturation,
                monetBackgroundSaturation,
                monetBackgroundLightness,
                pitchBlackTheme,
                accurateShades
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating modified colors", e)
            return null
        }
    }

    companion object {
        private val TAG: String = ThemeFragment::class.java.simpleName
    }
}