package com.drdisagree.colorblendr.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.ui.fragments.ColorsFragment;
import com.drdisagree.colorblendr.ui.fragments.SettingsFragment;
import com.drdisagree.colorblendr.ui.fragments.StylesFragment;
import com.drdisagree.colorblendr.ui.fragments.ThemeFragment;

public class FragmentUtil {

    public enum TAB_SELECTION {
        FROM_LEFT_TO_RIGHT,
        FROM_RIGHT_TO_LEFT,
        NONE
    }

    public static TAB_SELECTION getSlidingDirection(Fragment currentFragment, Fragment newFragment) {
        if (currentFragment == null) {
            return TAB_SELECTION.NONE;
        }

        boolean reverseAnimation;

        if (currentFragment instanceof ColorsFragment &&
                (newFragment instanceof ThemeFragment || newFragment instanceof StylesFragment || newFragment instanceof SettingsFragment)
        ) {
            reverseAnimation = false;
        } else if (currentFragment instanceof SettingsFragment &&
                (newFragment instanceof ThemeFragment || newFragment instanceof StylesFragment || newFragment instanceof ColorsFragment)
        ) {
            reverseAnimation = true;
        } else if (currentFragment instanceof ThemeFragment) {
            if (newFragment instanceof ColorsFragment) {
                reverseAnimation = true;
            } else if (newFragment instanceof StylesFragment || newFragment instanceof SettingsFragment) {
                reverseAnimation = false;
            } else {
                return TAB_SELECTION.NONE;
            }
        } else if (currentFragment instanceof StylesFragment) {
            if (newFragment instanceof SettingsFragment) {
                reverseAnimation = false;
            } else if (newFragment instanceof ColorsFragment || newFragment instanceof ThemeFragment) {
                reverseAnimation = true;
            } else {
                return TAB_SELECTION.NONE;
            }
        } else {
            return TAB_SELECTION.NONE;
        }

        return reverseAnimation ? TAB_SELECTION.FROM_RIGHT_TO_LEFT : TAB_SELECTION.FROM_LEFT_TO_RIGHT;
    }

    public static void setCustomAnimations(TAB_SELECTION direction, FragmentTransaction fragmentTransaction) {
        switch (direction) {
            case FROM_LEFT_TO_RIGHT -> fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
            case FROM_RIGHT_TO_LEFT -> fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
            case NONE -> fragmentTransaction.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.fade_out
            );
        }
    }
}
