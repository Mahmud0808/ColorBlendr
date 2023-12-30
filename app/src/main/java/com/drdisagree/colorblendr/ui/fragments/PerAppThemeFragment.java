package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_APPS;
import static com.drdisagree.colorblendr.common.Const.SHOW_PER_APP_THEME_WARN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentPerAppThemeBinding;
import com.drdisagree.colorblendr.ui.adapters.AppListAdapter;
import com.drdisagree.colorblendr.ui.models.AppInfo;
import com.drdisagree.colorblendr.utils.MiscUtil;
import com.drdisagree.colorblendr.utils.OverlayManager;

import java.util.ArrayList;
import java.util.List;

public class PerAppThemeFragment extends Fragment {

    private FragmentPerAppThemeBinding binding;
    private List<AppInfo> appList;
    private AppListAdapter adapter;
    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initAppList();
        }
    };
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!binding.searchBox.search.getText().toString().trim().isEmpty()) {
                binding.searchBox.clear.setVisibility(View.VISIBLE);
                filterList(binding.searchBox.search.getText().toString().trim());
            } else {
                binding.searchBox.clear.setVisibility(View.GONE);
                filterList("");
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPerAppThemeBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.per_app_theme, true, binding.header.toolbar);

        // Warning
        if (!RPrefs.getBoolean(SHOW_PER_APP_THEME_WARN, true)) {
            binding.warn.container.setVisibility(View.GONE);
        }
        binding.warn.close.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            RPrefs.putBoolean(SHOW_PER_APP_THEME_WARN, false);
            binding.warn.container.animate().translationX(binding.warn.container.getWidth() * 2f).alpha(0f).withEndAction(() -> binding.warn.container.setVisibility(View.GONE)).start();
        }, 50));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAppList();
    }

    private void initAppList() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.searchBox.search.removeTextChangedListener(textWatcher);

        new Thread(() -> {
            appList = getAllInstalledApps(requireContext());
            adapter = new AppListAdapter(appList);

            try {
                requireActivity().runOnUiThread(() -> {
                    binding.recyclerView.setAdapter(adapter);

                    binding.searchBox.search.addTextChangedListener(textWatcher);

                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    binding.searchBox.clear.setOnClickListener(v -> {
                        binding.searchBox.search.setText("");
                        binding.searchBox.clear.setVisibility(View.GONE);
                    });

                    if (!binding.searchBox.search.getText().toString().trim().isEmpty()) {
                        filterList(binding.searchBox.search.getText().toString().trim());
                    }
                });
            } catch (Exception ignored) {
                // Fragment was not attached to activity
            }
        }).start();
    }

    private void filterList(String query) {
        if (appList == null) {
            return;
        }

        List<AppInfo> startsWithNameList = new ArrayList<>();
        List<AppInfo> containsNameList = new ArrayList<>();
        List<AppInfo> startsWithPackageNameList = new ArrayList<>();
        List<AppInfo> containsPackageNameList = new ArrayList<>();

        for (AppInfo app : appList) {
            if (app.appName.toLowerCase().startsWith(query.toLowerCase())) {
                startsWithNameList.add(app);
            } else if (app.appName.toLowerCase().contains(query.toLowerCase())) {
                containsNameList.add(app);
            } else if (app.packageName.toLowerCase().startsWith(query.toLowerCase())) {
                startsWithPackageNameList.add(app);
            } else if (app.packageName.toLowerCase().contains(query.toLowerCase())) {
                containsPackageNameList.add(app);
            }
        }

        List<AppInfo> filteredList = new ArrayList<>();
        filteredList.addAll(startsWithNameList);
        filteredList.addAll(containsNameList);
        filteredList.addAll(startsWithPackageNameList);
        filteredList.addAll(containsPackageNameList);

        adapter = new AppListAdapter(filteredList);
        binding.recyclerView.setAdapter(adapter);
    }

    private static List<AppInfo> getAllInstalledApps(Context context) {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : applications) {
            String appName = appInfo.loadLabel(packageManager).toString();
            String packageName = appInfo.packageName;
            Drawable appIcon = appInfo.loadIcon(packageManager);
            boolean isSelected = OverlayManager.isOverlayEnabled(
                    String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
            );

            AppInfo app = new AppInfo(appName, packageName, appIcon);
            app.setSelected(isSelected);
            appList.add(app);
        }

        appList.sort((app1, app2) -> app1.appName.compareToIgnoreCase(app2.appName));

        return appList;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        requireActivity().registerReceiver(packageReceiver, filter);
    }

    @Override
    public void onPause() {
        try {
            requireActivity().unregisterReceiver(packageReceiver);
        } catch (Exception ignored) {
            // Receiver was not registered
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            requireActivity().unregisterReceiver(packageReceiver);
        } catch (Exception ignored) {
            // Receiver was not registered
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getParentFragmentManager().popBackStackImmediate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}