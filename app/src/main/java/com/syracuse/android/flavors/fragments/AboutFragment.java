package com.syracuse.android.flavors.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pkmmte.view.CircularImageView;
import com.syracuse.android.flavors.R;

/**
 * Settings Fragment - About me screen
 *
 * created by Rahul
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_about, container, false);
        CircularImageView circularImgSa = (CircularImageView)view.findViewById(R.id.sandeshImg);
        circularImgSa.setImageResource(R.drawable.sandeshimg);
        CircularImageView circularImgRa = (CircularImageView)view.findViewById(R.id.rahulImg);
        circularImgRa.setImageResource(R.drawable.rahulimg);
        return view;
    }


}
