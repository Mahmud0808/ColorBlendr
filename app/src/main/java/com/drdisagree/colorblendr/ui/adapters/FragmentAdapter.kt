package com.drdisagree.colorblendr.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentActivity: FragmentActivity, private val fragments: List<Fragment>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        if (position >= 0 && position < fragments.size) {
            return fragments[position]
        } else {
            throw IndexOutOfBoundsException("Invalid fragment position: $position")
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}
