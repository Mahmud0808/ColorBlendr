package com.drdisagree.colorblendr.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.data.common.Constant.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Utilities.accurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.clearAllOverriddenColors
import com.drdisagree.colorblendr.data.common.Utilities.customColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isColorOverriddenFor
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.manualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setAccurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setManualColorOverrideEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setTintedTextEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.tintedTextEnabled
import com.drdisagree.colorblendr.data.common.Utilities.updateColorAppliedTimestamp
import com.drdisagree.colorblendr.databinding.FragmentSettingsBinding
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.app.BackupRestore.backupDatabaseAndPrefs
import com.drdisagree.colorblendr.utils.app.BackupRestore.restoreDatabaseAndPrefs
import com.drdisagree.colorblendr.utils.app.MiscUtil.crossfade
import com.drdisagree.colorblendr.utils.app.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.app.parcelable
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.manager.OverlayManager.isOverlayEnabled
import com.drdisagree.colorblendr.utils.manager.OverlayManager.removeFabricatedColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var isMasterSwitchEnabled: Boolean = true
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val masterSwitch: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (!isMasterSwitchEnabled) {
                buttonView.isChecked = !isChecked
                return@OnCheckedChangeListener
            }

            setThemingEnabled(isChecked)
            setShizukuThemingEnabled(isChecked)
            setWirelessAdbThemingEnabled(isChecked)
            updateColorAppliedTimestamp()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(300)

                    withContext(Dispatchers.IO) {
                        if (isChecked) {
                            updateColors()
                        } else {
                            removeFabricatedColors()
                        }
                    }

                    isMasterSwitchEnabled = false
                    val isOverlayEnabled = isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM)
                            || isShizukuThemingEnabled()
                            || isWirelessAdbThemingEnabled()
                    buttonView.isChecked = isOverlayEnabled
                    isMasterSwitchEnabled = true

                    if (isChecked != isOverlayEnabled) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (_: Exception) {
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setToolbarTitle(requireContext(), R.string.settings, true, binding.header.toolbar)

        // ColorBlendr service
        binding.themingEnabled.setTitle(
            getString(
                R.string.app_service_title,
                getString(R.string.app_name)
            )
        )
        binding.themingEnabled.isSwitchChecked = (isThemingEnabled()
                && isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM))
                || isShizukuThemingEnabled()
                || isWirelessAdbThemingEnabled()

        binding.themingEnabled.setSwitchChangeListener(masterSwitch)

        // Accurate shades
        binding.accurateShades.isSwitchChecked = accurateShadesEnabled()
        binding.accurateShades.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            setAccurateShadesEnabled(isChecked)
            sharedViewModel.setBooleanState(MONET_ACCURATE_SHADES, isChecked)
            updateColors()
        }
        binding.accurateShades.setEnabled(isRootMode())

        // Pitch black theme
        binding.pitchBlackTheme.isSwitchChecked = pitchBlackThemeEnabled()
        binding.pitchBlackTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            setPitchBlackThemeEnabled(isChecked)
            updateColors()
        }
        binding.pitchBlackTheme.setEnabled(isRootMode())

        // Custom primary color
        binding.customPrimaryColor.isSwitchChecked = customColorEnabled()
        binding.customPrimaryColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            setCustomColorEnabled(isChecked)
            sharedViewModel.setVisibilityState(
                MONET_SEED_COLOR_ENABLED,
                if (isChecked) View.VISIBLE else View.GONE
            )
            if (!isChecked) {
                val wallpaperColorList = getWallpaperColorList()
                setSeedColorValue(
                    if (wallpaperColorList.isNotEmpty()) wallpaperColorList[0]
                    else Color.BLUE
                )
                updateColors()
            }
        }

        // Tint text color
        binding.tintTextColor.isSwitchChecked = tintedTextEnabled()
        binding.tintTextColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            setTintedTextEnabled(isChecked)
            updateColors()
        }
        binding.tintTextColor.setEnabled(isRootMode())

        // Override colors manually
        binding.overrideColorsManually.isSwitchChecked = manualColorOverrideEnabled()
        binding.overrideColorsManually.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                setManualColorOverrideEnabled(true)
            } else {
                if (shouldConfirmBeforeClearing()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.confirmation_title))
                        .setMessage(getString(R.string.this_cannot_be_undone))
                        .setPositiveButton(getString(android.R.string.ok)) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            setManualColorOverrideEnabled(false)
                            if (numColorsOverridden() != 0) {
                                clearAllOverriddenColors()
                                updateColors()
                            }
                        }
                        .setNegativeButton(getString(android.R.string.cancel)) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            binding.overrideColorsManually.isSwitchChecked = true
                        }
                        .show()
                } else {
                    setManualColorOverrideEnabled(false)
                    if (numColorsOverridden() != 0) {
                        clearAllOverriddenColors()
                        updateColors()
                    }
                }
            }
        }
        binding.overrideColorsManually.setEnabled(isRootMode())

        binding.backupRestore.container.setOnClickListener {
            binding.backupRestore.backupRestoreButtons.crossfade()
        }
        binding.backupRestore.backup.setOnClickListener {
            backupRestoreSettings(true)
        }
        binding.backupRestore.restore.setOnClickListener {
            backupRestoreSettings(false)
        }

        // About this app
        binding.about.setOnClickListener {
            HomeFragment.replaceFragment(AboutFragment())
        }

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.settings_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.advanced_settings -> {
                        HomeFragment.replaceFragment(SettingsAdvancedFragment())
                        true
                    }

                    R.id.privacy_policy -> {
                        HomeFragment.replaceFragment(PrivacyPolicyFragment())
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        arguments?.let { bundle ->
            bundle.parcelable<Uri>("data")?.let { data ->
                showRestoreDialog(data)
                bundle.remove("data")
            }
        }
    }


    private fun backupRestoreSettings(isBackingUp: Boolean) {
        val fileIntent = Intent().apply {
            action = if (isBackingUp) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "theme_config" + ".colorblendr")
        }

        if (isBackingUp) {
            startBackupActivityIntent.launch(fileIntent)
        } else {
            startRestoreActivityIntent.launch(fileIntent)
        }
    }

    private var startBackupActivityIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri = data?.data ?: return@registerForActivityResult

                CoroutineScope(Dispatchers.IO).launch {
                    val success = uri.backupDatabaseAndPrefs()

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Snackbar
                                .make(
                                    binding.getRoot(),
                                    getString(R.string.backup_success),
                                    Snackbar.LENGTH_LONG
                                )
                                .setAction(getString(R.string.dismiss)) { }
                                .show()
                        } else {
                            Snackbar
                                .make(
                                    binding.getRoot(),
                                    getString(R.string.backup_fail),
                                    Snackbar.LENGTH_INDEFINITE
                                )
                                .setAction(getString(R.string.retry)) {
                                    backupRestoreSettings(true)
                                }
                                .show()
                        }
                    }
                }
            }
        }

    private var startRestoreActivityIntent: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.data == null) return@registerForActivityResult

                showRestoreDialog(data.data)
            }
        }

    private fun showRestoreDialog(uri: Uri?) {
        if (uri == null) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirmation_title))
            .setMessage(getString(R.string.confirmation_desc))
            .setPositiveButton(getString(android.R.string.ok)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()

                CoroutineScope(Dispatchers.IO).launch {
                    val success = uri.restoreDatabaseAndPrefs()

                    withContext(Dispatchers.Main) {
                        if (success) {
                            updateColors()
                        } else {
                            Snackbar
                                .make(
                                    binding.getRoot(),
                                    getString(R.string.restore_fail),
                                    Snackbar.LENGTH_INDEFINITE
                                )
                                .setAction(getString(R.string.retry)) {
                                    backupRestoreSettings(false)
                                }
                                .show()
                        }

                        arguments?.remove("data")
                    }
                }
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss();
                arguments?.remove("data")
            }
            .show()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && isAdded) {
            getParentFragmentManager().popBackStackImmediate()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shouldConfirmBeforeClearing(): Boolean {
        return numColorsOverridden() > 5
    }

    private fun numColorsOverridden(): Int {
        var colorOverridden = 0

        systemPaletteNames.forEach { palettes ->
            palettes
                .asSequence()
                .filter { isColorOverriddenFor(it) }
                .forEach { _ -> colorOverridden++ }
        }

        return colorOverridden
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