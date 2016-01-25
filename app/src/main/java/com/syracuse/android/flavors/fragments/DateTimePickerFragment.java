package com.syracuse.android.flavors.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syracuse.android.flavors.R;


/**
 *  Fragment for date time picker
 *
 *  Created by Sandesh
 */
public class DateTimePickerFragment extends Fragment {

    private static final String ARG_OPTION = "argument_option";
    public DateTimePickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        int option = this.getArguments().getInt(ARG_OPTION);
        rootView = inflater.inflate(R.layout.fragment_date_time_picker, container, false);
        return rootView;
    }


    public static DateTimePickerFragment newInstance(int option) {
        DateTimePickerFragment fragment = new DateTimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_OPTION, option);
        fragment.setArguments(args);
        return fragment;

    }
}
