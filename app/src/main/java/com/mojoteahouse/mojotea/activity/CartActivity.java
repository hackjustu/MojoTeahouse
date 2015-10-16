package com.mojoteahouse.mojotea.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.CartSummaryItemAdapter;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.fragment.DeleteCartItemDialogFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartActivity extends AppCompatActivity implements View.OnClickListener, ActionMode.Callback,
        CartSummaryItemAdapter.CartSummaryItemClickListener, CartSummaryItemAdapter.CartSummaryItemLongClickListener,
        DeleteCartItemDialogFragment.DeleteCartItemListener {

    private static final String EXTRA_SELECTED_POSITIONS = "EXTRA_SELECTED_POSITIONS";
    private static final String TAG_DELETE_DIALOG = "TAG_DELETE_DIALOG";
    private static final String SPLIT_SYMBOL = "%";
    private static final int REQUEST_CODE_EDIT_ITEM = 1;

    private Toolbar toolbar;
    private CartSummaryItemAdapter itemAdapter;
    private SharedPreferences sharedPreferences;
    private ActionMode actionMode;
    private List<OrderItem> selectedOrderItems;
    private int totalQuantity;
    private double totalPrice;

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
        itemAdapter = new CartSummaryItemAdapter(this, new ArrayList<OrderItem>(), this, this);
        recyclerView.setAdapter(itemAdapter);

        Button checkoutButton = (Button) findViewById(R.id.bottom_action_button);
        checkoutButton.setOnClickListener(this);

        loadDataInBackground();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SparseBooleanArray selectedItemPositions = itemAdapter.getSelectedItemPositions();
        ArrayList<Integer> positionList = new ArrayList<>();
        for (int i = 0; i < selectedItemPositions.size(); i++) {
            positionList.add(selectedItemPositions.keyAt(i));
        }
        outState.putIntegerArrayList(EXTRA_SELECTED_POSITIONS, positionList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Integer> positionList = (ArrayList<Integer>) savedInstanceState.get(EXTRA_SELECTED_POSITIONS);
        if (!positionList.isEmpty()) {
            actionMode = startSupportActionMode(this);
            for (Integer position : positionList) {
                itemAdapter.setSelectionAtPosition(position, true);
            }
            actionMode.setTitle(String.format(getString(R.string.action_mode_title_format), itemAdapter.getSelectedCount()));
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_ITEM && resultCode == Activity.RESULT_OK) {
            loadDataInBackground();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_action_button:
                Intent intent = new Intent(this, PlaceOrderActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this, toolbar, getString(R.string.toolbar_transition));
                startActivity(intent, optionsCompat.toBundle());
                break;
        }
    }

    @Override
    public void onCartSummaryItemClicked(int position) {
        if (actionMode == null) {
            OrderItem orderItem = itemAdapter.getOrderItemAtPosition(position);
            Intent intent = new Intent(this, EditCartItemActivity.class);
            intent.putExtra(EditCartItemActivity.EXTRA_ORDER_ITEM_ID, orderItem.getOrderItemId());
            intent.putExtra(EditCartItemActivity.EXTRA_QUANTITY, orderItem.getQuantity());
            ArrayList<String> selectedToppings = new ArrayList<>();
            selectedToppings.addAll(orderItem.getSelectedToppingsList());
            intent.putStringArrayListExtra(EditCartItemActivity.EXTRA_SELECTED_TOPPINGS, selectedToppings);
            startActivityForResult(intent, REQUEST_CODE_EDIT_ITEM);
        } else {
            onListItemSelected(position);
        }
    }

    @Override
    public void onCartSummaryItemLongClicked(int position) {
        onListItemSelected(position);
    }

    @Override
    public void onDeleteConfirmed() {
        deleteSelectedOrderItems(selectedOrderItems);
        actionMode.finish();
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

    private void onListItemSelected(int position) {
        itemAdapter.setSelectionAtPosition(position, !itemAdapter.isSelectedAtPosition(position));
        boolean hasCheckedItems = itemAdapter.getSelectedCount() > 0;

        if (hasCheckedItems && actionMode == null) {
            actionMode = startSupportActionMode(this);
        } else if (!hasCheckedItems && actionMode != null) {
            actionMode.finish();
        }

        if (actionMode != null) {
            actionMode.setTitle(String.format(getString(R.string.action_mode_title_format), itemAdapter.getSelectedCount()));
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.cart_summary_action_mode_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                selectedOrderItems = new ArrayList<>();
                SparseBooleanArray selectedPositions = itemAdapter.getSelectedItemPositions();
                for (int i = 0; i < selectedPositions.size(); i++) {
                    selectedOrderItems.add(itemAdapter.getOrderItemAtPosition(selectedPositions.keyAt(i)));
                }

                DeleteCartItemDialogFragment fragment = (DeleteCartItemDialogFragment) getFragmentManager().findFragmentByTag(TAG_DELETE_DIALOG);
                if (fragment == null) {
                    fragment = DeleteCartItemDialogFragment.newInstance(getString(R.string.delete_selected_items_dialog_message));
                }
                fragment.show(getFragmentManager(), TAG_DELETE_DIALOG);
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        itemAdapter.clearSelection();
        actionMode = null;
    }

    private void deleteSelectedOrderItems(List<OrderItem> selectedOrderItems) {
        final Set<String> orderItemContentSet = sharedPreferences.getStringSet(
                MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, new HashSet<String>());
        totalQuantity = sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0);
        totalPrice = sharedPreferences.getFloat(MojoTeaApp.PREF_LOCAL_ORDER_TOTAL_PRICE, 0);

        for (OrderItem orderItem : selectedOrderItems) {
            for (final String orderItemString : orderItemContentSet) {
                if (orderItem.getOrderItemId().equals(orderItemString.split(SPLIT_SYMBOL)[0])) {
                    orderItemContentSet.remove(orderItemString);
                    totalQuantity -= orderItem.getQuantity();
                    totalPrice -= orderItem.getTotalPrice();
                    break;
                }
            }
        }

        ParseObject.unpinAllInBackground(MojoTeaApp.ORDER_ITEM_GROUP, selectedOrderItems, new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(CartActivity.this, R.string.delete_failed_error_message, Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, orderItemContentSet);
                    editor.putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, totalQuantity);
                    editor.putFloat(MojoTeaApp.PREF_LOCAL_ORDER_TOTAL_PRICE, (float) totalPrice);
                    editor.apply();

                    if (totalQuantity == 0) {
                        supportFinishAfterTransition();
                    } else {
                        getSupportActionBar().setTitle(String.format(getString(R.string.cart_toolbar_title), totalPrice));
                        loadDataInBackground();
                    }
                }
            }
        });
    }
}
