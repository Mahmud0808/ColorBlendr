package com.drdisagree.colorblendr.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.darkerLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.forcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.getTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.modeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.screenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.semiTransparentLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setDarkerLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setForcePitchBlackSettingsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setModeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setScreenOffColorUpdateEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.setSemiTransparentLauncherIconsEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.databinding.FragmentSettingsAdvancedBinding
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.app.SystemUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView

class SettingsAdvancedFragment : Fragment() {

    private lateinit var binding: FragmentSettingsAdvancedBinding
    private val hasPixelLauncher: Boolean = SystemUtil.isAppInstalled(PIXEL_LAUNCHER)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsAdvancedBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.advanced_settings, true, binding.header.toolbar)

        // Secondary color
        var monetSecondaryColor = getSecondaryColorValue()
        binding.secondaryColorPicker.isEnabled = customColorEnabled() && isRootMode()
        binding.secondaryColorPicker.previewColor = monetSecondaryColor
        binding.secondaryColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(10f)
                .withColor(monetSecondaryColor)
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView::class.java)
                .withListener { _: ColorPickerDialog?, color: Int ->
                    if (monetSecondaryColor != color) {
                        monetSecondaryColor = color
                        binding.secondaryColorPicker.previewColor = color

                        resetCustomStyleIfNotNull()
                        setSecondaryColorValue(monetSecondaryColor)

                        updateColors()
                    }
                }
                .show(getChildFragmentManager(), "secondaryColorPicker")
        }

        // Tertiary color
        var monetTertiaryColor = getTertiaryColorValue()
        binding.tertiaryColorPicker.isEnabled = customColorEnabled() && isRootMode()
        binding.tertiaryColorPicker.previewColor = monetTertiaryColor
        binding.tertiaryColorPicker.setOnClickListener {
            ColorPickerDialog()
                .withCornerRadius(10f)
                .withColor(monetTertiaryColor)
                .withAlphaEnabled(false)
                .withPicker(ImagePickerView::class.java)
                .withListener { _: ColorPickerDialog?, color: Int ->
                    if (monetTertiaryColor != color) {
                        monetTertiaryColor = color
                        binding.tertiaryColorPicker.previewColor = color

                        resetCustomStyleIfNotNull()
                        setTertiaryColorValue(monetTertiaryColor)

                        updateColors()
                    }
                }
                .show(getChildFragmentManager(), "tertiaryColorPicker")
        }

        // Update colors on screen off
        binding.screenOffUpdate.isSwitchChecked = screenOffColorUpdateEnabled()
        binding.screenOffUpdate.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            setScreenOffColorUpdateEnabled(isChecked)

            if (!isChecked) {
                updateColors()
            }
        }

        // Mode specific themes
        binding.modeSpecificThemes.isEnabled = isRootMode()
        binding.modeSpecificThemes.isSwitchChecked = modeSpecificThemesEnabled()
        binding.modeSpecificThemes.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            setModeSpecificThemesEnabled(isChecked)
            updateColors()
        }

        // Darker launcher icons
        binding.darkerLauncherIcons.isEnabled = isRootMode() && hasPixelLauncher
        binding.darkerLauncherIcons.isSwitchChecked = darkerLauncherIconsEnabled()
        binding.darkerLauncherIcons.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                savePixelLauncherInPerAppTheme()
            }
            setDarkerLauncherIconsEnabled(isChecked)
            updateColors()
        }

        // Semi-transparent launcher icons
        binding.semitransparentLauncher.isEnabled = isRootMode() && hasPixelLauncher
        binding.semitransparentLauncher.isSwitchChecked = semiTransparentLauncherIconsEnabled()
        binding.semitransparentLauncher.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                savePixelLauncherInPerAppTheme()
            }
            setSemiTransparentLauncherIconsEnabled(isChecked)
            updateColors()
        }

        // Semi-transparent launcher icons
        binding.pitchBlackSettingsWorkaround.isEnabled = isRootMode() && pitchBlackThemeEnabled()
        binding.pitchBlackSettingsWorkaround.visibility =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) View.VISIBLE else View.GONE
        binding.pitchBlackSettingsWorkaround.isSwitchChecked = forcePitchBlackSettingsEnabled()
        binding.pitchBlackSettingsWorkaround.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            setForcePitchBlackSettingsEnabled(isChecked)
            updateColors()
        }

        return binding.getRoot()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            getParentFragmentManager().popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePixelLauncherInPerAppTheme() {
        if (!hasPixelLauncher || !isRootMode()) {
            return
        }

        val selectedApps = getSelectedFabricatedApps()
        selectedApps[PIXEL_LAUNCHER] = true
        setSelectedFabricatedApps(selectedApps)
    }

    private fun updateColors() {
        CoroutineScope(Dispatchers.Main).launch {
            updateColorAppliedTimestamp()
            delay(300)
            withContext(Dispatchers.IO) {
                applyFabricatedColors()
            }
        }
    }
}