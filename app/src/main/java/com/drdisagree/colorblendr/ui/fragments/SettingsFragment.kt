package com.drdisagree.colorblendr.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS
import com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES
import com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED
import com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.common.Const.SHIZUKU_THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.TINT_TEXT_COLOR
import com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.common.Const.workingMethod
import com.drdisagree.colorblendr.config.RPrefs
import com.drdisagree.colorblendr.config.RPrefs.backupPrefs
import com.drdisagree.colorblendr.config.RPrefs.clearPref
import com.drdisagree.colorblendr.config.RPrefs.getBoolean
import com.drdisagree.colorblendr.config.RPrefs.getInt
import com.drdisagree.colorblendr.config.RPrefs.putBoolean
import com.drdisagree.colorblendr.config.RPrefs.putInt
import com.drdisagree.colorblendr.config.RPrefs.putLong
import com.drdisagree.colorblendr.config.RPrefs.restorePrefs
import com.drdisagree.colorblendr.databinding.FragmentSettingsBinding
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
import com.drdisagree.colorblendr.utils.ColorSchemeUtil.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.utils.ColorUtil.systemPaletteNames
import com.drdisagree.colorblendr.utils.MiscUtil.setToolbarTitle
import com.drdisagree.colorblendr.utils.OverlayManager.applyFabricatedColors
import com.drdisagree.colorblendr.utils.OverlayManager.isOverlayEnabled
import com.drdisagree.colorblendr.utils.OverlayManager.removeFabricatedColors
import com.drdisagree.colorblendr.utils.parcelable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private var sharedViewModel: SharedViewModel? = null
    private var isMasterSwitchEnabled: Boolean = true
    private val notShizukuMode: Boolean = workingMethod != Const.WorkMethod.SHIZUKU

    private val masterSwitch: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (!isMasterSwitchEnabled) {
                buttonView.isChecked = !isChecked
                return@OnCheckedChangeListener
            }

            putBoolean(THEMING_ENABLED, isChecked)
            putBoolean(SHIZUKU_THEMING_ENABLED, isChecked)
            putLong(MONET_LAST_UPDATED, System.currentTimeMillis())

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    delay(300)

                    withContext(Dispatchers.IO) {
                        if (isChecked) {
                            applyFabricatedColors(requireContext())
                        } else {
                            removeFabricatedColors(requireContext())
                        }
                    }

                    isMasterSwitchEnabled = false
                    val isOverlayEnabled: Boolean =
                        isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM) ||
                                getBoolean(SHIZUKU_THEMING_ENABLED, true)
                    buttonView.isChecked = isOverlayEnabled
                    isMasterSwitchEnabled = true

                    if (isChecked != isOverlayEnabled) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (ignored: Exception) {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
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
        binding.themingEnabled.isSwitchChecked = (getBoolean(THEMING_ENABLED, true) &&
                isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM)) ||
                getBoolean(SHIZUKU_THEMING_ENABLED, true)

        binding.themingEnabled.setSwitchChangeListener(masterSwitch)

        // Accurate shades
        binding.accurateShades.isSwitchChecked = getBoolean(MONET_ACCURATE_SHADES, true)
        binding.accurateShades.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            putBoolean(MONET_ACCURATE_SHADES, isChecked)
            sharedViewModel!!.setBooleanState(MONET_ACCURATE_SHADES, isChecked)
            applyFabricatedColors()
        }
        binding.accurateShades.setEnabled(notShizukuMode)

        // Pitch black theme
        binding.pitchBlackTheme.isSwitchChecked = getBoolean(MONET_PITCH_BLACK_THEME, false)
        binding.pitchBlackTheme.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            putBoolean(MONET_PITCH_BLACK_THEME, isChecked)
            applyFabricatedColors()
        }
        binding.pitchBlackTheme.setEnabled(notShizukuMode)

        // Custom primary color
        binding.customPrimaryColor.isSwitchChecked = getBoolean(MONET_SEED_COLOR_ENABLED, false)
        binding.customPrimaryColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            putBoolean(MONET_SEED_COLOR_ENABLED, isChecked)
            sharedViewModel!!.setVisibilityState(
                MONET_SEED_COLOR_ENABLED,
                if (isChecked) View.VISIBLE else View.GONE
            )
            if (!isChecked) {
                val wallpaperColors: String? = RPrefs.getString(WALLPAPER_COLOR_LIST, null)
                val wallpaperColorList: ArrayList<Int> = Const.GSON.fromJson(
                    wallpaperColors,
                    object : TypeToken<ArrayList<Int?>?>() {
                    }.type
                )
                putInt(MONET_SEED_COLOR, wallpaperColorList[0])
                applyFabricatedColors()
            }
        }

        // Tint text color
        binding.tintTextColor.isSwitchChecked = getBoolean(TINT_TEXT_COLOR, true)
        binding.tintTextColor.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            resetCustomStyleIfNotNull()
            putBoolean(TINT_TEXT_COLOR, isChecked)
            applyFabricatedColors()
        }
        binding.tintTextColor.setEnabled(notShizukuMode)

        // Override colors manually
        binding.overrideColorsManually.isSwitchChecked = getBoolean(MANUAL_OVERRIDE_COLORS, false)
        binding.overrideColorsManually.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                putBoolean(MANUAL_OVERRIDE_COLORS, true)
            } else {
                if (shouldConfirmBeforeClearing()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.confirmation_title))
                        .setMessage(getString(R.string.this_cannot_be_undone))
                        .setPositiveButton(getString(android.R.string.ok)) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            putBoolean(MANUAL_OVERRIDE_COLORS, false)
                            if (numColorsOverridden() != 0) {
                                clearCustomColors()
                                applyFabricatedColors()
                            }
                        }
                        .setNegativeButton(getString(android.R.string.cancel)) { dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                            binding.overrideColorsManually.isSwitchChecked = true
                        }
                        .show()
                } else {
                    putBoolean(MANUAL_OVERRIDE_COLORS, false)
                    if (numColorsOverridden() != 0) {
                        clearCustomColors()
                        applyFabricatedColors()
                    }
                }
            }
        }
        binding.overrideColorsManually.setEnabled(notShizukuMode)

        binding.backupRestore.container.setOnClickListener {
            crossfade(
                binding.backupRestore.backupRestoreButtons
            )
        }
        binding.backupRestore.backup.setOnClickListener {
            backupRestoreSettings(
                true
            )
        }
        binding.backupRestore.restore.setOnClickListener {
            backupRestoreSettings(
                false
            )
        }

        // About this app
        binding.about.setOnClickListener {
            HomeFragment.replaceFragment(
                AboutFragment()
            )
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
                        HomeFragment.replaceFragment(
                            SettingsAdvancedFragment()
                        )
                        true
                    }

                    else -> {
                        false
                    }
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

    private fun crossfade(view: View) {
        try {
            val animTime: Int = resources.getInteger(android.R.integer.config_mediumAnimTime)
            if (view.visibility == View.GONE) {
                view.alpha = 0f
                view.visibility = View.VISIBLE
                view.animate()
                    .alpha(1f)
                    .setDuration(animTime.toLong())
                    .setListener(null)
            } else {
                view.animate()
                    .alpha(0f)
                    .setDuration(animTime.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            try {
                                view.alpha = 0f
                                view.visibility = View.GONE
                            } catch (ignored: Exception) {
                            }
                        }
                    })
            }
        } catch (ignored: Exception) {
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
                if (data?.data == null) return@registerForActivityResult

                Executors.newSingleThreadExecutor().execute {
                    try {
                        backupPrefs(
                            appContext
                                .contentResolver
                                .openOutputStream(data.data!!)!!
                        )

                        Snackbar
                            .make(
                                binding.getRoot(),
                                getString(R.string.backup_success),
                                Snackbar.LENGTH_LONG
                            )
                            .setAction(getString(R.string.dismiss)) { }
                            .show()
                    } catch (exception: Exception) {
                        Snackbar
                            .make(
                                binding.getRoot(),
                                getString(R.string.backup_fail),
                                Snackbar.LENGTH_INDEFINITE
                            )
                            .setAction(getString(R.string.retry)) { backupRestoreSettings(true) }
                            .show()

                        Log.e(TAG, "startBackupActivityIntent: ", exception)
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
                    try {
                        restorePrefs(
                            appContext
                                .contentResolver
                                .openInputStream(uri)!!
                        )

                        withContext(Dispatchers.Main) {
                            try {
                                applyFabricatedColors(requireContext())
                            } catch (ignored: Exception) {
                            }
                        }
                    } catch (exception: Exception) {
                        withContext(Dispatchers.Main) {
                            Snackbar
                                .make(
                                    binding.getRoot(),
                                    getString(R.string.restore_fail),
                                    Snackbar.LENGTH_INDEFINITE
                                )
                                .setAction(getString(R.string.retry)) {
                                    backupRestoreSettings(
                                        false
                                    )
                                }
                                .show()

                            Log.e(TAG, "startBackupActivityIntent: ", exception)
                        }
                    } finally {
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
        if (item.itemId == android.R.id.home) {
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

        for (colorName: Array<String> in systemPaletteNames) {
            for (resource: String in colorName) {
                if (getInt(resource, Int.MIN_VALUE) != Int.MIN_VALUE) {
                    colorOverridden++
                }
            }
        }
        return colorOverridden
    }

    private fun applyFabricatedColors() {
        CoroutineScope(Dispatchers.Main).launch {
            putLong(
                MONET_LAST_UPDATED,
                System.currentTimeMillis()
            )
            delay(300)
            withContext(Dispatchers.IO) {
                try {
                    applyFabricatedColors(requireContext())
                } catch (ignored: Exception) {
                }
            }
        }
    }

    companion object {
        private val TAG: String = SettingsFragment::class.java.simpleName

        fun clearCustomColors() {
            for (colorName: Array<String> in systemPaletteNames) {
                for (resource: String in colorName) {
                    clearPref(resource)
                }
            }
        }
    }
}