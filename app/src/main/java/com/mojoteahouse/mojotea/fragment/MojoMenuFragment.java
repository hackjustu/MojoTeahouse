package com.mojoteahouse.mojotea.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.MojoMenuItemAdapter;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MojoMenuFragment extends Fragment {

    private MojoMenuItemAdapter adapter;

    public static MojoMenuFragment newInstance() {
        return new MojoMenuFragment();
    }

    public MojoMenuFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mojo_menu, container, false);
        Button button = (Button) view.findViewById(R.id.go_to_cart_button);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mojo_menu_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MojoMenuItemAdapter(getActivity(), new ArrayList<MojoMenu>());
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadMojoMenuesInBackground();
    }

    private void loadMojoMenuesInBackground() {
        ParseQuery<MojoMenu> mojoMenuQuery = MojoMenu.getQuery();
        mojoMenuQuery.findInBackground(new FindCallback<MojoMenu>() {
            @Override
            public void done(List<MojoMenu> mojoMenuList, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "Error query order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    adapter.updateMojoMenuList(mojoMenuList);
                }
            }
        });
    }
}
