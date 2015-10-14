package com.mojoteahouse.mojotea.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.CartSummaryItemAdapter;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements View.OnClickListener,
        CartSummaryItemAdapter.CartSummaryItemClickListener {

    private static final int REQUEST_CODE_EDIT_ITEM = 1;

    private Toolbar toolbar;
    private CartSummaryItemAdapter itemAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.summary_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        itemAdapter = new CartSummaryItemAdapter(this, new ArrayList<OrderItem>(), this);
        recyclerView.setAdapter(itemAdapter);

        Button checkoutButton = (Button) findViewById(R.id.check_out_button);
        checkoutButton.setOnClickListener(this);

        loadDataInBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0) == 0) {
            supportFinishAfterTransition();
        } else {
            getSupportActionBar().setTitle(String.format(getString(R.string.cart_toolbar_title),
                    sharedPreferences.getFloat(MojoTeaApp.PREF_LOCAL_ORDER_TOTAL_PRICE, 0)));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_out_button:
                Intent intent = new Intent(this, PlaceOrderActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, toolbar, getString(R.string.toolbar_transition));
                startActivity(intent, optionsCompat.toBundle());
                break;
        }
    }

    @Override
    public void onCartSummaryItemClicked(OrderItem orderItem) {
        Intent intent = new Intent(this, EditCartItemActivity.class);
        intent.putExtra(EditCartItemActivity.EXTRA_ORDER_ITEM_ID, orderItem.getOrderItemId());
        intent.putExtra(EditCartItemActivity.EXTRA_QUANTITY, orderItem.getQuantity());
        ArrayList<String> selectedToppings = new ArrayList<>();
        selectedToppings.addAll(orderItem.getSelectedToppingsList());
        intent.putStringArrayListExtra(EditCartItemActivity.EXTRA_SELECTED_TOPPINGS, selectedToppings);
        startActivityForResult(intent, REQUEST_CODE_EDIT_ITEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_ITEM && resultCode == Activity.RESULT_OK) {
            loadDataInBackground();
        }
    }

    private void loadDataInBackground() {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.whereEqualTo(OrderItem.ORDER_PLACED, false);
        orderItemQuery.findInBackground(new FindCallback<OrderItem>() {
            @Override
            public void done(List<OrderItem> orderItems, ParseException e) {
                if (e != null) {
                    Log.e("mojo", "get summary error: " + e.getMessage());
                    Toast.makeText(CartActivity.this, R.string.get_order_item_error_message, Toast.LENGTH_LONG).show();
                } else {
                    itemAdapter.updateOrderItemList(orderItems);
                }
            }
        });
    }
}
