package com.syracuse.android.flavors.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syracuse.android.flavors.R;

/**
 * Ack Fragment
 *
 * created by Rahul
 */
public class AckFragment extends Fragment {


    public AckFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ack, container, false);
    }


}
