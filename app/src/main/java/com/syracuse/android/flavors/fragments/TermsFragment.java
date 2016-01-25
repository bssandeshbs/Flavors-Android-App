package com.syracuse.android.flavors.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syracuse.android.flavors.R;

/**
 * Created by Rahul
 *
 * Fragment to view terms and conditions
 */
public class TermsFragment extends Fragment {


    public TermsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terms, container, false);
    }


}
