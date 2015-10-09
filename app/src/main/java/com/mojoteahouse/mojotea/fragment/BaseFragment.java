package com.mojoteahouse.mojotea.fragment;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;

public class BaseFragment extends Fragment {

    protected boolean isNetworkConnected;
    private ConnectivityManager connectivityManager;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
        if (!isNetworkConnected) {
            Toast.makeText(getActivity(), R.string.no_network_error_message, Toast.LENGTH_LONG).show();
        }
    }
}
