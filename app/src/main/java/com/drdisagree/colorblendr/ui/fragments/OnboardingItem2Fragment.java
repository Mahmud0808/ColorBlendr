package com.drdisagree.colorblendr.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingItem2Binding;
import com.drdisagree.colorblendr.utils.AppUtil;

import java.util.Map;

public class OnboardingItem2Fragment extends Fragment {

    private FragmentOnboardingItem2Binding binding;
    private boolean hasNotificationPermission = false;
    private boolean hasMediaPermission = false;
    private boolean hasStoragePermission = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem2Binding.inflate(inflater, container, false);

        initPermissionsAndViews();

        // Post notifications permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.postNotifications.setOnClickListener(v -> {
                if (hasNotificationPermission) {
                    return;
                }

                binding.postNotifications.setSelected(false);

                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
            });
        } else {
            binding.postNotifications.setVisibility(View.GONE);
        }

        // Read media images permission
        binding.readMediaImages.setOnClickListener(v -> {
            if (hasMediaPermission) {
                return;
            }

            binding.readMediaImages.setSelected(false);

            requestMediaPermission.launch(
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                            new String[]{
                                    Manifest.permission.READ_MEDIA_IMAGES
                            } :
                            new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            });
        });

        // All files access permission
        binding.allFilesAccess.setOnClickListener(v -> {
            binding.allFilesAccess.setSelected(false);

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
            requestAllFilesPermission.launch(intent);
        });

        return binding.getRoot();
    }

    private void initPermissionsAndViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS);
            hasMediaPermission = checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            hasNotificationPermission = true;
            hasMediaPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        hasStoragePermission = AppUtil.hasStoragePermission();

        if (hasNotificationPermission) {
            binding.postNotifications.setSelected(true);
        }

        if (hasMediaPermission) {
            binding.readMediaImages.setSelected(true);
        }

        if (hasStoragePermission) {
            binding.allFilesAccess.setSelected(true);
        }
    }

    private final ActivityResultLauncher<String> requestNotificationPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    binding.postNotifications.setSelected(true);
                    hasNotificationPermission = true;
                } else {
                    binding.postNotifications.setSelected(false);
                    hasNotificationPermission = false;
                }
            }
    );

    private final ActivityResultLauncher<String[]> requestMediaPermission = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::handleMediaPermissionsResult
    );

    private void handleMediaPermissionsResult(Map<String, Boolean> result) {
        for (Map.Entry<String, Boolean> pair : result.entrySet()) {
            if (!pair.getValue()) {
                binding.readMediaImages.setSelected(false);
                hasMediaPermission = false;
                return;
            }

            binding.readMediaImages.setSelected(true);
            hasMediaPermission = true;
        }
    }

    private final ActivityResultLauncher<Intent> requestAllFilesPermission = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (AppUtil.hasStoragePermission()) {
                    binding.allFilesAccess.setSelected(true);
                    hasStoragePermission = true;
                } else {
                    binding.allFilesAccess.setSelected(false);
                    hasStoragePermission = false;
                }
            }
    );

    private boolean checkSelfPermission(String permission) {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }
}