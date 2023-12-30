package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentSettingsBinding;
import com.drdisagree.colorblendr.ui.viewmodel.SharedViewModel;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private FragmentSettingsBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.settings, true, binding.header.toolbar);

        // Accurate shades
        binding.accurateShades.setSwitchChecked(RPrefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.accurateShades.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_ACCURATE_SHADES, isChecked);
            sharedViewModel.setBooleanState(MONET_ACCURATE_SHADES, isChecked);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    OverlayManager.applyFabricatedColors(requireContext());
                } catch (Exception ignored) {
                }
            }, 300);
        });

        // Pitch black theme
        binding.pitchBlackTheme.setSwitchChecked(RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false));
        binding.pitchBlackTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_PITCH_BLACK_THEME, isChecked);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    OverlayManager.applyFabricatedColors(requireContext());
                } catch (Exception ignored) {
                }
            }, 300);
        });

        // Custom primary color
        binding.customPrimaryColor.setSwitchChecked(RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false));
        binding.customPrimaryColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_SEED_COLOR_ENABLED, isChecked);
            sharedViewModel.setVisibilityState(MONET_SEED_COLOR_ENABLED, isChecked ? View.VISIBLE : View.GONE);

            if (!isChecked) {
                String wallpaperColors = RPrefs.getString(WALLPAPER_COLOR_LIST, null);
                ArrayList<Integer> wallpaperColorList = Const.GSON.fromJson(
                        wallpaperColors,
                        new TypeToken<ArrayList<Integer>>() {
                        }.getType()
                );
                RPrefs.putInt(MONET_SEED_COLOR, wallpaperColorList.get(0));
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 300);
            }
        });

        // Override colors manually
        binding.overrideColorsManually.setSwitchChecked(RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false));
        binding.overrideColorsManually.setSwitchChangeListener((buttonView, isChecked) -> {
            String[][] colorNames = ColorUtil.getColorNames();
            for (String[] colorName : colorNames) {
                for (String resource : colorName) {
                    RPrefs.clearPref(resource);
                }
            }
            RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, isChecked);

            if (!isChecked) {
                RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
                new Handler(Looper.getMainLooper()).postDelayed(() -> OverlayManager.applyFabricatedColors(requireContext()), 300);
            }
        });

        binding.backupRestore.backup.setOnClickListener(v -> backupRestoreSettings(true));
        binding.backupRestore.restore.setOnClickListener(v -> backupRestoreSettings(false));

        // About this app
        binding.about.setOnClickListener(v -> HomeFragment.replaceFragment(new AboutFragment()));

        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    private void backupRestoreSettings(boolean isBackingUp) {
        Intent fileIntent = new Intent();
        fileIntent.setAction(isBackingUp ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        fileIntent.putExtra(Intent.EXTRA_TITLE, "theme_config" + ".colorblendr");
        if (isBackingUp) {
            startBackupActivityIntent.launch(fileIntent);
        } else {
            startRestoreActivityIntent.launch(fileIntent);
        }
    }

    ActivityResultLauncher<Intent> startBackupActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null || data.getData() == null) return;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            RPrefs.backupPrefs(
                                    Objects.requireNonNull(
                                            ColorBlendr.getAppContext()
                                                    .getContentResolver()
                                                    .openOutputStream(data.getData())
                                    )
                            );

                            Snackbar.make(
                                            binding.getRoot(),
                                            getString(R.string.backup_success),
                                            Snackbar.LENGTH_LONG
                                    )
                                    .setAction(
                                            getString(R.string.dismiss),
                                            v -> {
                                            }
                                    )
                                    .show();
                        } catch (Exception exception) {
                            Snackbar.make(
                                            binding.getRoot(),
                                            getString(R.string.backup_fail),
                                            Snackbar.LENGTH_INDEFINITE
                                    )
                                    .setAction(
                                            getString(R.string.retry),
                                            v -> backupRestoreSettings(true)
                                    )
                                    .show();

                            Log.e(TAG, "startBackupActivityIntent: ", exception);
                        }
                    });
                }
            });

    ActivityResultLauncher<Intent> startRestoreActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null || data.getData() == null) return;

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.confirmation_title))
                            .setMessage(getString(R.string.confirmation_desc))
                            .setPositiveButton(getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            try {
                                                RPrefs.clearAllPrefs();

                                                RPrefs.restorePrefs(
                                                        Objects.requireNonNull(
                                                                ColorBlendr.getAppContext()
                                                                        .getContentResolver()
                                                                        .openInputStream(data.getData())
                                                        )
                                                );

                                                try {
                                                    ProcessPhoenix.triggerRebirth(requireContext());
                                                } catch (Exception ignored) {
                                                }
                                            } catch (Exception exception) {
                                                Snackbar.make(
                                                                binding.getRoot(),
                                                                getString(R.string.restore_fail),
                                                                Snackbar.LENGTH_INDEFINITE
                                                        )
                                                        .setAction(
                                                                getString(R.string.retry),
                                                                v -> backupRestoreSettings(false)
                                                        )
                                                        .show();

                                                Log.e(TAG, "startBackupActivityIntent: ", exception);
                                            }
                                        });
                                    })
                            .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getParentFragmentManager().popBackStackImmediate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}