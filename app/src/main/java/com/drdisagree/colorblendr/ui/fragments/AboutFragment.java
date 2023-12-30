package com.drdisagree.colorblendr.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.databinding.FragmentAboutBinding;
import com.drdisagree.colorblendr.utils.MiscUtil;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.about_this_app_title, true, binding.header.toolbar);

        binding.github.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mahmud0808/ColorBlendr"))));

        binding.versionCodes.setText(
                getString(
                        R.string.version_codes,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                )
        );

        binding.developer.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Mahmud0808"))));

        binding.buymeacoffee.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/drdisagree"))));

        return binding.getRoot();
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