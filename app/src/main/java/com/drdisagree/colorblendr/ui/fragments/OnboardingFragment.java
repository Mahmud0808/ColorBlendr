package com.drdisagree.colorblendr.ui.fragments;

import static com.drdisagree.colorblendr.common.Const.WORKING_METHOD;

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

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingBinding;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.ui.adapters.OnboardingAdapter;
import com.drdisagree.colorblendr.utils.AppUtil;

public class OnboardingFragment extends Fragment {

    private static final String TAG = OnboardingFragment.class.getSimpleName();
    private FragmentOnboardingBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);

        OnboardingAdapter adapter = new OnboardingAdapter(
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
        });

        binding.btnNext.setOnClickListener(v -> {
            if (binding.viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
                if (!AppUtil.permissionsGranted(requireContext())) {
                    Toast.makeText(requireContext(), "Please grant all permissions to proceed", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (WORKING_METHOD == Const.WORK_METHOD.NULL) {
                    Toast.makeText(requireContext(), "Select a method to proceed", Toast.LENGTH_SHORT).show();
                } else if (WORKING_METHOD == Const.WORK_METHOD.ROOT) {
                    RootServiceProvider rootServiceProvider = new RootServiceProvider(
                            requireContext(),
                            requireActivity().getSupportFragmentManager(),
                            ((ViewGroup) binding.getRoot().getParent()).getId()
                    );
                    rootServiceProvider.startRootService();
                } else if (WORKING_METHOD == Const.WORK_METHOD.SHIZUKU) {
                    Toast.makeText(requireContext(), "Shizuku is not supported yet", Toast.LENGTH_SHORT).show();
                } else if (WORKING_METHOD == Const.WORK_METHOD.XPOSED) {
                    Toast.makeText(requireContext(), "Xposed is not supported yet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Unknown method", Toast.LENGTH_SHORT).show();
                }

                return;
            }

            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        });

        binding.btnPrev.setOnClickListener(v -> binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true));

        registerOnBackInvokedCallback();

        return binding.getRoot();
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