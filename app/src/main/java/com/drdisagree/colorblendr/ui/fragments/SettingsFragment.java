package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_SYSTEM;
import static com.drdisagree.colorblendr.common.Const.MANUAL_OVERRIDE_COLORS;
import static com.drdisagree.colorblendr.common.Const.MONET_ACCURATE_SHADES;
import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_PITCH_BLACK_THEME;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR;
import static com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED;
import static com.drdisagree.colorblendr.common.Const.SHIZUKU_THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.THEMING_ENABLED;
import static com.drdisagree.colorblendr.common.Const.TINT_TEXT_COLOR;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.CompoundButton;
import android.widget.Toast;

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
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel;
import com.drdisagree.colorblendr.utils.ColorUtil;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private FragmentSettingsBinding binding;
    private SharedViewModel sharedViewModel;
    private boolean isMasterSwitchEnabled = true;
    private static final String[][] colorNames = ColorUtil.getColorNames();
    private final boolean notShizukuMode = Const.getWorkingMethod() != Const.WORK_METHOD.SHIZUKU;
    private final CompoundButton.OnCheckedChangeListener masterSwitch = (buttonView, isChecked) -> {
        if (!isMasterSwitchEnabled) {
            buttonView.setChecked(!isChecked);
            return;
        }

        RPrefs.putBoolean(THEMING_ENABLED, isChecked);
        RPrefs.putBoolean(SHIZUKU_THEMING_ENABLED, isChecked);
        RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (isChecked) {
                    OverlayManager.applyFabricatedColors(requireContext());
                } else {
                    OverlayManager.removeFabricatedColors(requireContext());
                }

                isMasterSwitchEnabled = false;
                boolean isOverlayEnabled = OverlayManager.isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM) ||
                        RPrefs.getBoolean(SHIZUKU_THEMING_ENABLED, true);
                buttonView.setChecked(isOverlayEnabled);
                isMasterSwitchEnabled = true;

                if (isChecked != isOverlayEnabled) {
                    Toast.makeText(
                                    requireContext(),
                                    getString(R.string.something_went_wrong),
                                    Toast.LENGTH_SHORT
                            )
                            .show();
                }
            } catch (Exception ignored) {
            }
        }, 300);
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.settings, true, binding.header.toolbar);

        // ColorBlendr service
        binding.themingEnabled.setTitle(getString(R.string.app_service_title, getString(R.string.app_name)));
        binding.themingEnabled.setSwitchChecked(
                (RPrefs.getBoolean(THEMING_ENABLED, true) &&
                        OverlayManager.isOverlayEnabled(FABRICATED_OVERLAY_NAME_SYSTEM)) ||
                        RPrefs.getBoolean(SHIZUKU_THEMING_ENABLED, true)
        );
        binding.themingEnabled.setSwitchChangeListener(masterSwitch);

        // Accurate shades
        binding.accurateShades.setSwitchChecked(RPrefs.getBoolean(MONET_ACCURATE_SHADES, true));
        binding.accurateShades.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_ACCURATE_SHADES, isChecked);
            sharedViewModel.setBooleanState(MONET_ACCURATE_SHADES, isChecked);
            applyFabricatedColors();
        });
        binding.accurateShades.setEnabled(notShizukuMode);

        // Pitch black theme
        binding.pitchBlackTheme.setSwitchChecked(RPrefs.getBoolean(MONET_PITCH_BLACK_THEME, false));
        binding.pitchBlackTheme.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(MONET_PITCH_BLACK_THEME, isChecked);
            applyFabricatedColors();
        });
        binding.pitchBlackTheme.setEnabled(notShizukuMode);

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
                applyFabricatedColors();
            }
        });

        // Tint text color
        binding.tintTextColor.setSwitchChecked(RPrefs.getBoolean(TINT_TEXT_COLOR, true));
        binding.tintTextColor.setSwitchChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(TINT_TEXT_COLOR, isChecked);
            applyFabricatedColors();
        });
        binding.tintTextColor.setEnabled(notShizukuMode);

        // Override colors manually
        binding.overrideColorsManually.setSwitchChecked(RPrefs.getBoolean(MANUAL_OVERRIDE_COLORS, false));
        binding.overrideColorsManually.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, true);
            } else {
                if (shouldConfirmBeforeClearing()) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.confirmation_title))
                            .setMessage(getString(R.string.this_cannot_be_undone))
                            .setPositiveButton(getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, false);
                                        if (numColorsOverridden() != 0) {
                                            clearCustomColors();
                                            applyFabricatedColors();
                                        }
                                    })
                            .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {
                                dialog.dismiss();
                                binding.overrideColorsManually.setSwitchChecked(true);
                            })
                            .show();
                } else {
                    RPrefs.putBoolean(MANUAL_OVERRIDE_COLORS, false);
                    if (numColorsOverridden() != 0) {
                        clearCustomColors();
                        applyFabricatedColors();
                    }
                }
            }
        });
        binding.overrideColorsManually.setEnabled(notShizukuMode);

        binding.backupRestore.container.setOnClickListener(v -> crossfade(binding.backupRestore.backupRestoreButtons));
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

    private void crossfade(View view) {
        try {
            int animTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
            if (view.getVisibility() == View.GONE) {
                view.setAlpha(0f);
                view.setVisibility(View.VISIBLE);
                view.animate()
                        .alpha(1f)
                        .setDuration(animTime)
                        .setListener(null);
            } else {
                view.animate()
                        .alpha(0f)
                        .setDuration(animTime)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                try {
                                    view.setAlpha(0f);
                                    view.setVisibility(View.GONE);
                                } catch (Exception ignored) {
                                }
                            }
                        });
            }
        } catch (Exception ignored) {
        }
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
                                                    OverlayManager.applyFabricatedColors(requireContext());
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

    public static void clearCustomColors() {
        for (String[] colorName : colorNames) {
            for (String resource : colorName) {
                RPrefs.clearPref(resource);
            }
        }
    }

    private boolean shouldConfirmBeforeClearing() {
        return numColorsOverridden() > 5;
    }

    private int numColorsOverridden() {
        int colorOverridden = 0;
        for (String[] colorName : colorNames) {
            for (String resource : colorName) {
                if (RPrefs.getInt(resource, Integer.MIN_VALUE) != Integer.MIN_VALUE) {
                    colorOverridden++;
                }
            }
        }
        return colorOverridden;
    }

    private void applyFabricatedColors() {
        RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                OverlayManager.applyFabricatedColors(requireContext());
            } catch (Exception ignored) {
            }
        }, 300);
    }
}