package com.syracuse.android.flavors.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.syracuse.android.flavors.fragments.MyEventsFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for Events created by user
 */
public class MyEventsPageAdapter extends FragmentPagerAdapter {

    List<Map<String,String>> myEventSet;

    public MyEventsPageAdapter(FragmentManager fm, List<Map<String,String>> mDataSet) {
        super(fm);
        myEventSet = mDataSet;
    }

    @Override
    public Fragment getItem(int position) {
       return MyEventsFragment.newInstance((HashMap<String,?>)myEventSet.get(position));
    }

    @Override
    public int getCount() {
        return myEventSet.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        HashMap<String,?> movie = (HashMap<String,?>) myEventSet.get(position);
        String name = (String) movie.get("EventName");
        return name.toUpperCase(l);
    }
}
