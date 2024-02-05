package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.FIRST_RUN;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.ActivityMainBinding;
import com.drdisagree.colorblendr.ui.fragments.HomeFragment;
import com.drdisagree.colorblendr.ui.fragments.OnboardingFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupEdgeToEdge();

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            if (RPrefs.getBoolean(FIRST_RUN, true) ||
                    Const.getWorkingMethod() == Const.WORK_METHOD.NULL ||
                    !getIntent().getBooleanExtra("success", false)
            ) {
                replaceFragment(new OnboardingFragment(), false);
            } else {
                replaceFragment(new HomeFragment(), false);
            }
        }
    }

    private void setupEdgeToEdge() {
        try {
            ((AppBarLayout) findViewById(R.id.appBarLayout)).setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(getApplicationContext()));
        } catch (Exception ignored) {
        }

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup viewGroup = getWindow().getDecorView().findViewById(android.R.id.content);
            ViewCompat.setOnApplyWindowInsetsListener(viewGroup, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                v.setPadding(
                        params.leftMargin + insets.left,
                        0,
                        params.rightMargin + insets.right,
                        0
                );
                params.topMargin = 0;
                params.bottomMargin = 0;
                v.setLayoutParams(params);

                return windowInsets;
            });
        }
    }

    public static void replaceFragment(Fragment fragment, boolean animate) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (animate) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }
        fragmentTransaction.replace(
                R.id.fragmentContainer,
                fragment
        );

        if (Objects.equals(tag, HomeFragment.class.getSimpleName())) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (!Objects.equals(tag, OnboardingFragment.class.getSimpleName())) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}