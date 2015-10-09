package com.mojoteahouse.mojotea.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mojoteahouse.mojotea.R;

public class CartCustomerDetailFragment extends Fragment {

    public static CartCustomerDetailFragment newInstance() {
        CartCustomerDetailFragment fragment = new CartCustomerDetailFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public CartCustomerDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_customer_detail, container, false);

        return view;
    }


}
