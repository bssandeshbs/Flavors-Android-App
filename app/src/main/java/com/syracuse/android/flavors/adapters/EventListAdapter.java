package com.syracuse.android.flavors.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.syracuse.android.flavors.fragments.EventFragment;
import java.util.Locale;

/* Adapter to show list of events */
public class EventListAdapter extends FragmentPagerAdapter {

    public EventListAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return EventFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale locale = Locale.getDefault();
        String name;

        switch (position){
            case 0:
                name = "All Events";
                break;
            case 1:
                name = "Trending Events";
                break;
            case 2:
                name = "Upcoming Events";
                break;
            default:
                name = "All Events";
                break;
        }
        return name.toUpperCase(locale);
    }
}
