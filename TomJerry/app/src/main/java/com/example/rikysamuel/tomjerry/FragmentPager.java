package com.example.rikysamuel.tomjerry;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Rikysamuel on 3/6/2015.
 */
public class FragmentPager extends FragmentPagerAdapter {
    public FragmentPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (i==0){
            return new fragmentUI();
        }
        if (i==1){
            return new fragmentStart();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
