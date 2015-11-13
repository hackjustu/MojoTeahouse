package com.mojoteahouse.mojotea.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mojoteahouse.mojotea.R;

public class AboutFragment extends BaseFragment {

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }
}
