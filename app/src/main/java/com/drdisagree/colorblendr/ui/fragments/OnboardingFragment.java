package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.FIRST_RUN;
import static com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.config.RPrefs;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingBinding;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootConnectionProvider;
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider;
import com.drdisagree.colorblendr.service.ShizukuConnection;
import com.drdisagree.colorblendr.ui.activities.MainActivity;
import com.drdisagree.colorblendr.ui.adapters.OnboardingAdapter;
import com.drdisagree.colorblendr.utils.AppUtil;
import com.drdisagree.colorblendr.utils.FabricatedUtil;
import com.drdisagree.colorblendr.utils.ShizukuUtil;
import com.drdisagree.colorblendr.utils.WallpaperUtil;

import java.util.ArrayList;

public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;
    private OnboardingAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);

        adapter = new OnboardingAdapter(
                getChildFragmentManager(),
                requireActivity().getLifecycle()
        );

        adapter.addFragment(new OnboardingItem1Fragment());
        adapter.addFragment(new OnboardingItem2Fragment());
        adapter.addFragment(new OnboardingItem3Fragment());

        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                animateBackButton(position);
                changeContinueButtonText(position);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
                if (!AppUtil.permissionsGranted(requireContext())) {
                    Toast.makeText(requireContext(), R.string.grant_all_permissions, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Const.WORKING_METHOD == Const.WORK_METHOD.NULL) {
                    Toast.makeText(requireContext(), R.string.select_method, Toast.LENGTH_SHORT).show();
                } else if (Const.WORKING_METHOD == Const.WORK_METHOD.ROOT) {
                    checkRootConnection();
                } else if (Const.WORKING_METHOD == Const.WORK_METHOD.SHIZUKU) {
                    checkShizukuConnection();
                }

                return;
            }

            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        });

        binding.btnPrev.setOnClickListener(v -> binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true));

        registerOnBackInvokedCallback();

        return binding.getRoot();
    }

    private void checkRootConnection() {
        RootConnectionProvider.builder(requireContext())
                .runOnSuccess(new MethodInterface() {
                    @Override
                    public void run() {
                        goToHomeFragment();
                    }
                }).run();
    }

    private void checkShizukuConnection() {
        if (ShizukuUtil.isShizukuAvailable()) {
            ShizukuUtil.requestShizukuPermission(requireActivity(), granted -> {
                if (granted) {
                    ShizukuUtil.bindUserService(
                            ShizukuUtil.getUserServiceArgs(ShizukuConnection.class),
                            ShizukuConnectionProvider.serviceConnection
                    );
                    goToHomeFragment();
                } else {
                    Toast.makeText(
                            ColorBlendr.getAppContext(),
                            R.string.shizuku_service_not_found,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        } else {
            Toast.makeText(
                    ColorBlendr.getAppContext(),
                    R.string.shizuku_service_not_found,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void goToHomeFragment() {
        Const.saveWorkingMethod(Const.WORKING_METHOD);
        WallpaperUtil.getAndSaveWallpaperColors(requireContext());
        FabricatedUtil.getAndSaveSelectedFabricatedApps(requireContext());
        RPrefs.putBoolean(FIRST_RUN, false);
        MainActivity.replaceFragment(new HomeFragment(), true);
    }

    private void animateBackButton(int position) {
        int duration = 300;

        if (position == 0 && binding.btnPrev.getVisibility() == View.VISIBLE) {
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(duration);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.btnPrev.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            binding.btnPrev.startAnimation(fadeOut);
        } else if (position != 0 && binding.btnPrev.getVisibility() != View.VISIBLE) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(duration);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.btnPrev.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            binding.btnPrev.startAnimation(fadeIn);
        }
    }

    private void changeContinueButtonText(int position) {
        if (position == adapter.getItemCount() - 1) {
            binding.btnNext.setText(R.string.start);

            new Thread(() -> {
                try {
                    if (AppUtil.hasStoragePermission()) {
                        ArrayList<Integer> wallpaperColors = WallpaperUtil.getWallpaperColors(requireContext());
                        RPrefs.putString(WALLPAPER_COLOR_LIST, Const.GSON.toJson(wallpaperColors));
                    }
                } catch (Exception ignored) {
                }
            }).start();
        } else {
            binding.btnNext.setText(R.string.btn_continue);
        }
    }

    private void registerOnBackInvokedCallback() {
        requireActivity().getOnBackPressedDispatcher().addCallback(
                requireActivity(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        onBackPressed();
                    }
                });
    }

    private void onBackPressed() {
        if (binding.viewPager.getCurrentItem() == 0) {
            requireActivity().finish();
        } else {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true);
        }
    }
}