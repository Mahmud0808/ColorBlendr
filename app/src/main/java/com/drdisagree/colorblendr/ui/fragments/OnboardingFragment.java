package com.drdisagree.colorblendr.ui.fragments;

import static android.content.Context.RECEIVER_EXPORTED;
import static com.drdisagree.colorblendr.common.Const.ACTION_HOOK_CHECK_REQUEST;
import static com.drdisagree.colorblendr.common.Const.ACTION_HOOK_CHECK_RESULT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.databinding.FragmentOnboardingBinding;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.extension.ObservableVariable;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.ui.adapters.OnboardingAdapter;
import com.drdisagree.colorblendr.utils.AppUtil;

import java.util.Objects;

public class OnboardingFragment extends Fragment {

    private static final String TAG = OnboardingFragment.class.getSimpleName();
    private FragmentOnboardingBinding binding;
    IntentFilter intentFilterHookedSystemUI = new IntentFilter();
    private final Handler hookCheckerHandler = new Handler(Looper.getMainLooper());
    private static final ObservableVariable<Boolean> isHookSuccessful = new ObservableVariable<>();
    private final BroadcastReceiver receiverHookedSystemui = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), ACTION_HOOK_CHECK_RESULT)) {
                isHookSuccessful.setValue(true);
                hookCheckerHandler.removeCallbacksAndMessages(null);
            }
        }
    };

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

                if (Const.WORKING_METHOD == Const.WORK_METHOD.NULL) {
                    Toast.makeText(requireContext(), "Select a method to proceed", Toast.LENGTH_SHORT).show();
                } else if (Const.WORKING_METHOD == Const.WORK_METHOD.ROOT) {
                    checkRootConnection();
                } else if (Const.WORKING_METHOD == Const.WORK_METHOD.SHIZUKU) {
                    checkShizukuConnection();
                } else if (Const.WORKING_METHOD == Const.WORK_METHOD.XPOSED) {
                    checkXposedConnection();
                }

                return;
            }

            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
        });

        binding.btnPrev.setOnClickListener(v -> binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1, true));

        registerXposedHookChecker();
        registerOnBackInvokedCallback();

        return binding.getRoot();
    }

    private void checkRootConnection() {
        RootServiceProvider rootServiceProvider = new RootServiceProvider(requireContext());
        rootServiceProvider.runOnSuccess(new MethodInterface() {
            @Override
            public void run() {
                Const.saveWorkingMethod(Const.WORKING_METHOD);
                goToHomeFragment();
            }
        });
        rootServiceProvider.startRootService();
    }

    private void checkShizukuConnection() {
        Toast.makeText(requireContext(), "Shizuku is not supported yet", Toast.LENGTH_SHORT).show();
    }

    private void checkXposedConnection() {
        if (isHookSuccessful.getValue()) {
            Const.saveWorkingMethod(Const.WORKING_METHOD);
            goToHomeFragment();
        } else {
            Toast.makeText(requireContext(), "Xposed hook not working", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerXposedHookChecker() {
        isHookSuccessful.setValue(false);

        intentFilterHookedSystemUI.addAction(ACTION_HOOK_CHECK_RESULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiverHookedSystemui, intentFilterHookedSystemUI, RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(receiverHookedSystemui, intentFilterHookedSystemUI);
        }

        hookCheckerHandler.post(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(1600, 800) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (isHookSuccessful.getValue()) {
                            cancel();
                        }
                    }

                    @Override
                    public void onFinish() {
                    }
                }.start();

                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        requireContext().sendBroadcast(new Intent().setAction(ACTION_HOOK_CHECK_REQUEST));
                    } catch (Exception ignored) {
                    }
                });

                hookCheckerHandler.postDelayed(this, 1000);
            }
        });
    }

    private void goToHomeFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(
                ((ViewGroup) binding.getRoot().getParent()).getId(),
                new HomeFragment(),
                HomeFragment.class.getSimpleName()
        );
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.commit();
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

    @Override
    public void onDestroy() {
        try {
            requireContext().unregisterReceiver(receiverHookedSystemui);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}