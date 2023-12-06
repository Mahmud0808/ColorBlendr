package com.drdisagree.colorblendr.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.databinding.ActivityMainBinding;
import com.drdisagree.colorblendr.ui.adapters.FragmentAdapter;
import com.drdisagree.colorblendr.ui.fragments.AboutFragment;
import com.drdisagree.colorblendr.ui.fragments.ToolsFragment;
import com.drdisagree.colorblendr.ui.fragments.StylingFragment;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

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
        binding.viewPager.setCurrentItem(1, false);

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
    }
}