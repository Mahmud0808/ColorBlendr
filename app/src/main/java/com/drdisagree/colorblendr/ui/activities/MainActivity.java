package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.TAB_SELECTED_INDEX;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.ActivityMainBinding;
import com.drdisagree.colorblendr.service.BackgroundService;
import com.drdisagree.colorblendr.ui.adapters.FragmentAdapter;
import com.drdisagree.colorblendr.ui.fragments.AboutFragment;
import com.drdisagree.colorblendr.ui.fragments.StylingFragment;
import com.drdisagree.colorblendr.ui.fragments.ToolsFragment;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.header.logo.setText(getString(R.string.tab_app_name, getString(R.string.app_name)));
        binding.header.appbarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            int totalScrollRange = appBarLayout1.getTotalScrollRange();
            float alpha = 1.0f - Math.abs((float) verticalOffset / totalScrollRange * 2f);
            binding.header.logo.setAlpha(Math.max(0.0f, Math.min(1.0f, alpha)));
        });

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new AboutFragment());
        fragments.add(new StylingFragment());
        fragments.add(new ToolsFragment());
        binding.viewPager.setAdapter(new FragmentAdapter(this, fragments));
        binding.viewPager.setCurrentItem(RPrefs.getInt(TAB_SELECTED_INDEX, 1), false);

        new TabLayoutMediator(
                binding.header.tabLayout,
                binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.tab_about);
                    } else if (position == 1) {
                        tab.setText(R.string.tab_styling);
                    } else if (position == 2) {
                        tab.setText(R.string.tab_tools);
                    }
                }
        ).attach();

        binding.header.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                RPrefs.putInt(TAB_SELECTED_INDEX, tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                RPrefs.putInt(TAB_SELECTED_INDEX, tab.getPosition());
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (AppUtil.permissionsGranted(this) && AppUtil.hasStoragePermission()) {
                if (!Const.isBackgroundServiceRunning) {
                    startForegroundService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
                }
            } else {
                requestPermissionsLauncher.launch(AppUtil.REQUIRED_PERMISSIONS);
            }
        }, 3000);
    }

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            this::handlePermissionsResult
    );

    private void handlePermissionsResult(Map<String, Boolean> result) {
        for (Map.Entry<String, Boolean> pair : result.entrySet()) {
            if (!pair.getValue()) {
                showGeneralPermissionSnackbar(pair.getKey());
                return;
            }
        }

        if (!AppUtil.hasStoragePermission()) {
            showStoragePermissionSnackbar();
            return;
        }

        if (!Const.isBackgroundServiceRunning) {
            startForegroundService(new Intent(ColorBlendr.getAppContext(), BackgroundService.class));
        }
    }

    private void showGeneralPermissionSnackbar(String permission) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                R.string.permission_must_be_granted,
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction(R.string.grant, v -> {
            requestPermissionsLauncher.launch(new String[]{permission});
            snackbar.dismiss();
        });
        snackbar.show();
    }

    private void showStoragePermissionSnackbar() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                R.string.file_access_permission_required,
                Snackbar.LENGTH_INDEFINITE
        );
        snackbar.setAction(R.string.grant, v -> {
            AppUtil.requestStoragePermission(MainActivity.this);
            snackbar.dismiss();
        });
        snackbar.show();
    }
}