package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.activity.EditMojoItemActivity;
import com.mojoteahouse.mojotea.adapter.MojoMenuItemAdapter;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.Topping;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MojoMenuFragment extends Fragment implements MojoMenuItemAdapter.MojoMenuItemClickListener, View.OnClickListener {

    private static final int REQUEST_CODE_EDIT_ITEM = 1;

    private MojoMenuItemAdapter adapter;
    private Button goToCartButton;
    private SharedPreferences sharedPreferences;
    private List<Long> orderItemIdList;
    private int totalCount;

    public static MojoMenuFragment newInstance() {
        MojoMenuFragment fragment = new MojoMenuFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public MojoMenuFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderItemIdList = new ArrayList<>();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mojo_menu, container, false);

        goToCartButton = (Button) view.findViewById(R.id.go_to_cart_button);
        goToCartButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.mojo_menu_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MojoMenuItemAdapter(getActivity(), new ArrayList<MojoMenu>(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.startLayoutAnimation();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDataInBackground();
    }

    @Override
    public void onItemClicked(MojoMenu mojoMenu) {
        Intent intent = new Intent(getActivity(), EditMojoItemActivity.class);
        intent.putExtra(EditMojoItemActivity.EXTRA_MOJO_MENU_NAME, mojoMenu.getName());
        intent.putExtra(EditMojoItemActivity.EXTRA_MOJO_MENU_PRICE, mojoMenu.getPrice());
        ArrayList<Integer> availableToppingIdList = new ArrayList<>();
        availableToppingIdList.addAll(mojoMenu.getToppingList());
        intent.putIntegerArrayListExtra(EditMojoItemActivity.EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS, availableToppingIdList);
        startActivityForResult(intent, REQUEST_CODE_EDIT_ITEM);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_cart_button:

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_ITEM && resultCode == Activity.RESULT_OK) {
            int quantity = data.getIntExtra(EditMojoItemActivity.EXTRA_QUANTITY, 1);
            long orderItemId = data.getLongExtra(EditMojoItemActivity.EXTRA_ORDER_ITEM_ID, 0);
            updateButtonText(quantity);
            orderItemIdList.add(orderItemId);
        }
    }

    private void loadDataInBackground() {
        ParseQuery<MojoMenu> mojoMenuQuery = MojoMenu.getQuery();
        mojoMenuQuery.findInBackground(new FindCallback<MojoMenu>() {
            @Override
            public void done(List<MojoMenu> mojoMenuList, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "Error query order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    ParseObject.pinAllInBackground(MojoTeaApp.MOJO_MENU_GROUP, mojoMenuList);
                    adapter.updateMojoMenuList(mojoMenuList);
                }
            }
        });

        ParseQuery<Topping> toppingQuery = Topping.getQuery();
        toppingQuery.findInBackground(new FindCallback<Topping>() {
            @Override
            public void done(List<Topping> toppingList, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground(MojoTeaApp.TOPPING_GROUP, toppingList);
                }
            }
        });

//        if (sharedPreferences.contains(MojoTeaApp.PREF_EXISTING_ORDER)) {
//
//        }
    }

    private void updateButtonText(int quantity) {
        totalCount += quantity;
        goToCartButton.setText(String.format(getString(R.string.go_to_cart_button_text), totalCount));
        goToCartButton.setVisibility(totalCount > 0 ? View.VISIBLE : View.GONE);

//        if (totalCount > 0 && !sharedPreferences.contains(MojoTeaApp.PREF_EXISTING_ORDER)) {
//            Order order = new Order();
//
//            sharedPreferences.edit()
//        }
    }
}
