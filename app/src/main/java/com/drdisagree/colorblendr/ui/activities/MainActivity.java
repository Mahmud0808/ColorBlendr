package com.drdisagree.colorblendr.ui.activities;

import static com.drdisagree.colorblendr.common.Const.WORKING_METHOD;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.databinding.ActivityMainBinding;
import com.drdisagree.colorblendr.extension.MethodInterface;
import com.drdisagree.colorblendr.provider.RootServiceProvider;
import com.drdisagree.colorblendr.ui.fragments.HomeFragment;
import com.drdisagree.colorblendr.ui.fragments.OnboardingFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            if (WORKING_METHOD == Const.WORK_METHOD.NULL) {
                replaceFragment(new OnboardingFragment());
            } else if (WORKING_METHOD == Const.WORK_METHOD.ROOT) {
                RootServiceProvider rootServiceProvider = new RootServiceProvider(this);
                rootServiceProvider.runOnSuccess(new MethodInterface() {
                    @Override
                    public void run() {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(
                                ((ViewGroup) binding.getRoot().getParent()).getId(),
                                new HomeFragment(),
                                HomeFragment.class.getSimpleName()
                        );
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commit();
                    }
                });
                rootServiceProvider.startRootService();
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment, tag);

        if (Objects.equals(tag, HomeFragment.class.getSimpleName())) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (!Objects.equals(tag, OnboardingFragment.class.getSimpleName())) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }
}