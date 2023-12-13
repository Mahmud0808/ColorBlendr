package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentToolsBinding;
import com.drdisagree.colorblendr.ui.viewmodel.SharedViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.util.Objects;
import java.util.concurrent.Executors;

public class ToolsFragment extends Fragment {

    private static final String TAG = ToolsFragment.class.getSimpleName();
    private FragmentToolsBinding binding;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);

        binding.accurateShades.setSwitchChecked(RPrefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.accurateShades.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_ACCURATE_SHADES, isChecked);
            sharedViewModel.setBooleanState(MONET_ACCURATE_SHADES, isChecked);
        });

        binding.pitchBlackTheme.setSwitchChecked(RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false));
        binding.pitchBlackTheme.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(MONET_PITCH_BLACK_THEME, isChecked));

        binding.customPrimaryColor.setSwitchChecked(RPrefs.getBoolean(MONET_SEED_COLOR_ENABLED, false));
        binding.customPrimaryColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_SEED_COLOR_ENABLED, isChecked);
            sharedViewModel.setVisibilityState(MONET_SEED_COLOR_ENABLED, isChecked ? View.VISIBLE : View.GONE);
        });

        binding.overrideColorsManually.setSwitchChecked(RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false));
        binding.overrideColorsManually.setSwitchChangeListener((buttonView, isChecked) -> RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, isChecked));

        binding.backupRestore.backup.setOnClickListener(v -> backupRestoreSettings(true));
        binding.backupRestore.restore.setOnClickListener(v -> backupRestoreSettings(false));

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
}