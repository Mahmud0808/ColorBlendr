package com.drdisagree.colorblendr.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.drdisagree.colorblendr.R;
import com.drdisagree.colorblendr.ui.fragments.AboutFragment;
import com.drdisagree.colorblendr.ui.fragments.ColorsFragment;
import com.drdisagree.colorblendr.ui.fragments.PerAppThemeFragment;
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

        TAB_SELECTION direction;

        if (isInGroup1(currentFragment) && !isInGroup1(newFragment)) {
            direction = TAB_SELECTION.FROM_LEFT_TO_RIGHT;
        } else if (isInGroup4(currentFragment) && !isInGroup4(newFragment)) {
            direction = TAB_SELECTION.FROM_RIGHT_TO_LEFT;
        } else if (isInGroup2(currentFragment)) {
            if (isInGroup1(newFragment)) {
                direction = TAB_SELECTION.FROM_RIGHT_TO_LEFT;
            } else if (isInGroup3(newFragment) || isInGroup4(newFragment)) {
                direction = TAB_SELECTION.FROM_LEFT_TO_RIGHT;
            } else {
                return TAB_SELECTION.NONE;
            }
        } else if (isInGroup3(currentFragment)) {
            if (isInGroup4(newFragment)) {
                direction = TAB_SELECTION.FROM_LEFT_TO_RIGHT;
            } else if (isInGroup1(newFragment) || isInGroup2(newFragment)) {
                direction = TAB_SELECTION.FROM_RIGHT_TO_LEFT;
            } else {
                return TAB_SELECTION.NONE;
            }
        } else {
            return TAB_SELECTION.NONE;
        }

        return direction;
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

    private static boolean isInGroup1(Fragment fragment) {
        return fragment instanceof ColorsFragment || fragment instanceof PerAppThemeFragment;
    }

    private static boolean isInGroup2(Fragment fragment) {
        return fragment instanceof ThemeFragment;
    }

    private static boolean isInGroup3(Fragment fragment) {
        return fragment instanceof StylesFragment;
    }

    private static boolean isInGroup4(Fragment fragment) {
        return fragment instanceof SettingsFragment || fragment instanceof AboutFragment;
    }
}
