package com.n00blife.lockit.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.n00blife.lockit.fragments.IntroFragment;

public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return IntroFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return 4;
    }
}
