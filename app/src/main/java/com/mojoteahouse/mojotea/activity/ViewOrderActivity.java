package com.mojoteahouse.mojotea.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.CartSummaryItemAdapter;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ViewOrderActivity extends AppCompatActivity implements CartSummaryItemAdapter.CartSummaryItemClickListener {

    public static final String EXTRA_ORDER_TIME = "EXTRA_ORDER_TIME";

    private CartSummaryItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.order_item_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        itemAdapter = new CartSummaryItemAdapter(this, new ArrayList<OrderItem>(), this);
        recyclerView.setAdapter(itemAdapter);

        loadOrderInBackground(getIntent().getStringExtra(EXTRA_ORDER_TIME));
    }

    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
    }


    @Override
    public void onCartSummaryItemClicked(OrderItem orderItem) {
        Intent intent = new Intent(this, ViewOrderItemActivity.class);
        intent.putExtra(ViewOrderItemActivity.EXTRA_ORDER_ITEM_ID, orderItem.getOrderItemId());
        startActivity(intent);
    }

    private void loadOrderInBackground(String orderTime) {
        ParseQuery<Order> orderQuery = Order.getQuery();
        orderQuery.fromLocalDatastore();
        orderQuery.whereEqualTo(Order.ORDER_TIME, orderTime);
        orderQuery.getFirstInBackground(new GetCallback<Order>() {
            @Override
            public void done(Order order, ParseException e) {
                if (e != null) {
                    Toast.makeText(ViewOrderActivity.this, R.string.get_order_history_error_message, Toast.LENGTH_LONG).show();
                } else {
                    double totalPrice = order.getTotalPrice();
                    getSupportActionBar().setTitle(String.format(getString(R.string.view_order_toolbar_title), totalPrice));
                    List<OrderItem> orderItemList = order.getOrderItemList();
                    itemAdapter.updateOrderItemList(orderItemList);
                }
            }
        });
    }
}
