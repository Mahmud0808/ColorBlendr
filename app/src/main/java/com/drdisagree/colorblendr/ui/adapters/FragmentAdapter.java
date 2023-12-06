package com.drdisagree.colorblendr.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class FragmentAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments;

    public FragmentAdapter(FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        } else {
            throw new IndexOutOfBoundsException("Invalid fragment position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
