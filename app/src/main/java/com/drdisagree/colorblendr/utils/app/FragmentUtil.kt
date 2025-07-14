package com.drdisagree.colorblendr.utils.app

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.fragments.AboutFragment
import com.drdisagree.colorblendr.ui.fragments.ColorPaletteFragment
import com.drdisagree.colorblendr.ui.fragments.ColorsFragment
import com.drdisagree.colorblendr.ui.fragments.PerAppThemeFragment
import com.drdisagree.colorblendr.ui.fragments.PrivacyPolicyFragment
import com.drdisagree.colorblendr.ui.fragments.SettingsAdvancedFragment
import com.drdisagree.colorblendr.ui.fragments.SettingsFragment
import com.drdisagree.colorblendr.ui.fragments.StylesFragment
import com.drdisagree.colorblendr.ui.fragments.ThemeFragment

object FragmentUtil {
    fun getSlidingDirection(currentFragment: Fragment?, newFragment: Fragment): TabSelection {
        if (currentFragment == null) {
            return TabSelection.NONE
        }

        val direction = if (isInGroup1(currentFragment) && !isInGroup1(newFragment)) {
            TabSelection.FROM_LEFT_TO_RIGHT
        } else if (isInGroup4(currentFragment) && !isInGroup4(
                newFragment
            )
        ) {
            TabSelection.FROM_RIGHT_TO_LEFT
        } else if (isInGroup2(currentFragment)) {
            if (isInGroup1(newFragment)) {
                TabSelection.FROM_RIGHT_TO_LEFT
            } else if (isInGroup3(newFragment) || isInGroup4(
                    newFragment
                )
            ) {
                TabSelection.FROM_LEFT_TO_RIGHT
            } else {
                return TabSelection.NONE
            }
        } else if (isInGroup3(currentFragment)) {
            if (isInGroup4(newFragment)) {
                TabSelection.FROM_LEFT_TO_RIGHT
            } else if (isInGroup1(newFragment) || isInGroup2(
                    newFragment
                )
            ) {
                TabSelection.FROM_RIGHT_TO_LEFT
            } else {
                return TabSelection.NONE
            }
        } else {
            return TabSelection.NONE
        }

        return direction
    }

    fun setCustomAnimations(direction: TabSelection, fragmentTransaction: FragmentTransaction) {
        when (direction) {
            TabSelection.FROM_LEFT_TO_RIGHT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )

            TabSelection.FROM_RIGHT_TO_LEFT -> fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_left,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )

            TabSelection.NONE -> fragmentTransaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
        }
    }

    private fun isInGroup1(fragment: Fragment): Boolean {
        return fragment is ColorsFragment ||
                fragment is PerAppThemeFragment ||
                fragment is ColorPaletteFragment
    }

    private fun isInGroup2(fragment: Fragment): Boolean {
        return fragment is ThemeFragment
    }

    private fun isInGroup3(fragment: Fragment): Boolean {
        return fragment is StylesFragment
    }

    private fun isInGroup4(fragment: Fragment): Boolean {
        return fragment is SettingsFragment ||
                fragment is SettingsAdvancedFragment ||
                fragment is AboutFragment ||
                fragment is PrivacyPolicyFragment
    }

    enum class TabSelection {
        FROM_LEFT_TO_RIGHT,
        FROM_RIGHT_TO_LEFT,
        NONE
    }
}
