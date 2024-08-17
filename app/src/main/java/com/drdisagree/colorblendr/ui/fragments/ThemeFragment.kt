package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.drdisagree.colorblendr.common.Const.MONET_STYLE
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.config.RPrefs.clearPref
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.databinding.FragmentThemeBinding
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.stringToEnumMonetStyle
import com.drdisagree.colorblendr.utils.ColorUtil.generateModifiedColors
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.SystemUtil.isDarkMode

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
    private val notShizukuMode: Boolean = workingMethod != Const.WorkMethod.SHIZUKU

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemeBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.theme, true, binding.header.toolbar)

        // Color preview titles
        binding.colorAccent1.title.setText(R.string.primary)
        binding.colorAccent2.title.setText(R.string.secondary)
        binding.colorAccent3.title.setText(R.string.tertiary)
        binding.colorNeutral1.title.setText(R.string.neutral_1)
        binding.colorNeutral2.title.setText(R.string.neutral_2)

        // Monet primary accent saturation
        binding.accentSaturation.seekbarProgress = getInt(MONET_ACCENT_SATURATION, 100)
        binding.accentSaturation.setOnSeekbarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.accentSaturation.setSelectedProgress()
                monetAccentSaturation[0] = progress
                updatePreviewColors()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                monetAccentSaturation[0] = seekBar.progress
                putInt(MONET_ACCENT_SATURATION, monetAccentSaturation[0])
                putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        applyFabricatedColors(requireContext())
                    } catch (ignored: Exception) {
                    }
                }, 200)
            }
        })

        // Long Click Reset
        binding.accentSaturation.setResetClickListener {
            monetAccentSaturation[0] = 100
            updatePreviewColors()
            clearPref(MONET_ACCENT_SATURATION)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }, 200)
            true
        }
        binding.accentSaturation.setEnabled(notShizukuMode)

        // Monet background saturation
        binding.backgroundSaturation.seekbarProgress = getInt(MONET_BACKGROUND_SATURATION, 100)
        binding.backgroundSaturation.setOnSeekbarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.backgroundSaturation.setSelectedProgress()
                monetBackgroundSaturation[0] = progress
                updatePreviewColors()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                monetBackgroundSaturation[0] = seekBar.progress
                putInt(MONET_BACKGROUND_SATURATION, monetBackgroundSaturation[0])
                putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        applyFabricatedColors(requireContext())
                    } catch (ignored: Exception) {
                    }
                }, 200)
            }
        })

        // Reset button
        binding.backgroundSaturation.setResetClickListener {
            monetBackgroundSaturation[0] = 100
            updatePreviewColors()
            clearPref(MONET_BACKGROUND_SATURATION)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }, 200)
            true
        }
        binding.backgroundSaturation.setEnabled(notShizukuMode)

        // Monet background lightness
        binding.backgroundLightness.seekbarProgress = getInt(MONET_BACKGROUND_LIGHTNESS, 100)
        binding.backgroundLightness.setOnSeekbarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.backgroundLightness.setSelectedProgress()
                monetBackgroundLightness[0] = progress
                updatePreviewColors()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                monetBackgroundLightness[0] = seekBar.progress
                putInt(MONET_BACKGROUND_LIGHTNESS, monetBackgroundLightness[0])
                putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        applyFabricatedColors(requireContext())
                    } catch (ignored: Exception) {
                    }
                }, 200)
            }
        })

        // Long Click Reset
        binding.backgroundLightness.setResetClickListener {
            monetBackgroundLightness[0] = 100
            updatePreviewColors()
            clearPref(MONET_BACKGROUND_LIGHTNESS)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }, 200)
            true
        }
        binding.backgroundLightness.setEnabled(notShizukuMode)

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updatePreviewColors()
    }

    private fun updatePreviewColors() {
        val colorPalette: ArrayList<ArrayList<Int>>? = generateModifiedColors(
            monetAccentSaturation[0],
            monetBackgroundSaturation[0],
            monetBackgroundLightness[0],
            getBoolean(MONET_PITCH_BLACK_THEME, false),
            getBoolean(MONET_ACCURATE_SHADES, true)
        )

        if (colorPalette != null) {
            val isDarkMode: Boolean = isDarkMode

            binding.colorAccent1.colorContainer.setHalfCircleColor(colorPalette[0][4])
            binding.colorAccent1.colorContainer.setFirstQuarterCircleColor(colorPalette[0][5])
            binding.colorAccent1.colorContainer.setSecondQuarterCircleColor(colorPalette[0][6])
            binding.colorAccent1.colorContainer.setSquareColor(colorPalette[0][if (!isDarkMode) 3 else 9])
            binding.colorAccent1.colorContainer.setPadding(8f)
            binding.colorAccent1.colorContainer.invalidateColors()

            binding.colorAccent2.colorContainer.setHalfCircleColor(colorPalette[1][4])
            binding.colorAccent2.colorContainer.setFirstQuarterCircleColor(colorPalette[1][5])
            binding.colorAccent2.colorContainer.setSecondQuarterCircleColor(colorPalette[1][6])
            binding.colorAccent2.colorContainer.setSquareColor(colorPalette[1][if (!isDarkMode) 3 else 9])
            binding.colorAccent2.colorContainer.setPadding(8f)
            binding.colorAccent2.colorContainer.invalidateColors()

            binding.colorAccent3.colorContainer.setHalfCircleColor(colorPalette[2][4])
            binding.colorAccent3.colorContainer.setFirstQuarterCircleColor(colorPalette[2][5])
            binding.colorAccent3.colorContainer.setSecondQuarterCircleColor(colorPalette[2][6])
            binding.colorAccent3.colorContainer.setSquareColor(colorPalette[2][if (!isDarkMode) 3 else 9])
            binding.colorAccent3.colorContainer.setPadding(8f)
            binding.colorAccent3.colorContainer.invalidateColors()

            binding.colorNeutral1.colorContainer.setHalfCircleColor(colorPalette[3][4])
            binding.colorNeutral1.colorContainer.setFirstQuarterCircleColor(colorPalette[3][5])
            binding.colorNeutral1.colorContainer.setSecondQuarterCircleColor(colorPalette[3][6])
            binding.colorNeutral1.colorContainer.setSquareColor(colorPalette[3][if (!isDarkMode) 3 else 9])
            binding.colorNeutral1.colorContainer.setPadding(8f)
            binding.colorNeutral1.colorContainer.invalidateColors()

            binding.colorNeutral2.colorContainer.setHalfCircleColor(colorPalette[4][4])
            binding.colorNeutral2.colorContainer.setFirstQuarterCircleColor(colorPalette[4][5])
            binding.colorNeutral2.colorContainer.setSecondQuarterCircleColor(colorPalette[4][6])
            binding.colorNeutral2.colorContainer.setSquareColor(colorPalette[4][if (!isDarkMode) 3 else 9])
            binding.colorNeutral2.colorContainer.setPadding(8f)
            binding.colorNeutral2.colorContainer.invalidateColors()
        }
    }

    private fun generateModifiedColors(
        monetAccentSaturation: Int,
        monetBackgroundSaturation: Int,
        monetBackgroundLightness: Int,
        pitchBlackTheme: Boolean,
        accurateShades: Boolean
    ): ArrayList<ArrayList<Int>>? {
        try {
            return generateModifiedColors(
                requireContext(),
                stringToEnumMonetStyle(
                    requireContext(),
                    RPrefs.getString(MONET_STYLE, getString(R.string.monet_tonalspot))!!
                ),
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
        private val TAG: String = ThemeFragment::class.java.getSimpleName()
    }
}