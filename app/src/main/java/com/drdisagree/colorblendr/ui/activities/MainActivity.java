package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.TAB_SELECTED_INDEX;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.ActivityMainBinding;
import com.drdisagree.colorblendr.ui.adapters.FragmentAdapter;
import com.drdisagree.colorblendr.ui.fragments.AboutFragment;
import com.drdisagree.colorblendr.ui.fragments.StylingFragment;
import com.drdisagree.colorblendr.ui.fragments.ToolsFragment;
import com.google.android.material.tabs.TabLayout;
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
    }
}