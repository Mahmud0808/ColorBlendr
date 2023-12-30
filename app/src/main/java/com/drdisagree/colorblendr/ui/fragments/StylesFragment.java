package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.MONET_LAST_UPDATED;
import static com.drdisagree.colorblendr.common.Const.MONET_STYLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentStylesBinding;
import com.drdisagree.colorblendr.utils.MiscUtil;

public class StylesFragment extends Fragment {

    private FragmentStylesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStylesBinding.inflate(inflater, container, false);

        MiscUtil.setToolbarTitle(requireContext(), R.string.styles, true, binding.header.toolbar);

        String selectedStyle = RPrefs.getString(MONET_STYLE, null);

        binding.monetNeutral.setSelected(getString(R.string.monet_neutral).equals(selectedStyle));
        binding.monetNeutral.setOnClickListener(v -> {
            binding.monetNeutral.setSelected(true);
            unSelectOthers(binding.monetNeutral);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetNeutral.applyColorScheme();
        });

        binding.monetMonochrome.setSelected(getString(R.string.monet_monochrome).equals(selectedStyle));
        binding.monetMonochrome.setOnClickListener(v -> {
            binding.monetMonochrome.setSelected(true);
            unSelectOthers(binding.monetMonochrome);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetMonochrome.applyColorScheme();
        });

        binding.monetTonalspot.setSelected(getString(R.string.monet_tonalspot).equals(selectedStyle));
        binding.monetTonalspot.setOnClickListener(v -> {
            binding.monetTonalspot.setSelected(true);
            unSelectOthers(binding.monetTonalspot);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetTonalspot.applyColorScheme();
        });

        binding.monetVibrant.setSelected(getString(R.string.monet_vibrant).equals(selectedStyle));
        binding.monetVibrant.setOnClickListener(v -> {
            binding.monetVibrant.setSelected(true);
            unSelectOthers(binding.monetVibrant);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetVibrant.applyColorScheme();
        });

        binding.monetRainbow.setSelected(getString(R.string.monet_rainbow).equals(selectedStyle));
        binding.monetRainbow.setOnClickListener(v -> {
            binding.monetRainbow.setSelected(true);
            unSelectOthers(binding.monetRainbow);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetRainbow.applyColorScheme();
        });

        binding.monetExpressive.setSelected(getString(R.string.monet_expressive).equals(selectedStyle));
        binding.monetExpressive.setOnClickListener(v -> {
            binding.monetExpressive.setSelected(true);
            unSelectOthers(binding.monetExpressive);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetExpressive.applyColorScheme();
        });

        binding.monetFidelity.setSelected(getString(R.string.monet_fidelity).equals(selectedStyle));
        binding.monetFidelity.setOnClickListener(v -> {
            binding.monetFidelity.setSelected(true);
            unSelectOthers(binding.monetFidelity);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetFidelity.applyColorScheme();
        });

        binding.monetContent.setSelected(getString(R.string.monet_content).equals(selectedStyle));
        binding.monetContent.setOnClickListener(v -> {
            binding.monetContent.setSelected(true);
            unSelectOthers(binding.monetContent);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetContent.applyColorScheme();
        });

        binding.monetFruitsalad.setSelected(getString(R.string.monet_fruitsalad).equals(selectedStyle));
        binding.monetFruitsalad.setOnClickListener(v -> {
            binding.monetFruitsalad.setSelected(true);
            unSelectOthers(binding.monetFruitsalad);
            RPrefs.putLong(MONET_LAST_UPDATED, System.currentTimeMillis());
            binding.monetFruitsalad.applyColorScheme();
        });

        return binding.getRoot();
    }

    private void unSelectOthers(ViewGroup viewGroup) {
        ViewGroup[] viewGroups = new ViewGroup[]{
                binding.monetNeutral,
                binding.monetMonochrome,
                binding.monetTonalspot,
                binding.monetVibrant,
                binding.monetRainbow,
                binding.monetExpressive,
                binding.monetFidelity,
                binding.monetContent,
                binding.monetFruitsalad
        };

        for (ViewGroup view : viewGroups) {
            if (view != viewGroup) {
                view.setSelected(false);
            }
        }
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