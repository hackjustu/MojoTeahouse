package com.mojoteahouse.mojotea.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.fragment.CartCustomerDetailFragment;
import com.mojoteahouse.mojotea.fragment.CartSummaryFragment;
import com.mojoteahouse.mojotea.fragment.ConfirmOrderDialogFragment;
import com.mojoteahouse.mojotea.fragment.PlacingOrderDialogFragment;
import com.mojoteahouse.mojotea.util.DataUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements View.OnClickListener,
        ConfirmOrderDialogFragment.PlaceOrderListener {

    public static final String NAME_SPLIT_SYMBOL = "   ";
    public static final String TOPPING_SPLIT_SYMBOL = ", ";

    private static final String TAG_SUMMARY = "TAG_SUMMARY";
    private static final String TAG_CUSTOMER_DETAIL = "TAG_CUSTOMER_DETAIL";
    private static final String TAG_CONFIRM_ORDER_DIALOG = "TAG_CONFIRM_ORDER_DIALOG";
    private static final String TAG_PLACING_ORDER_DIALOG = "TAG_PLACING_ORDER_DIALOG";

    private ViewSwitcher buttonViewSwitcher;
    private SharedPreferences sharedPreferences;
    private Date deliverTime;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.cart_toolbar_title);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        buttonViewSwitcher = (ViewSwitcher) findViewById(R.id.button_view_switcher);
        Button nextButton = (Button) findViewById(R.id.next_button);
        Button backButton = (Button) findViewById(R.id.back_button);
        Button placeOrderButton = (Button) findViewById(R.id.place_order_button);
        nextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        placeOrderButton.setOnClickListener(this);

        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        updateFragment(TAG_SUMMARY, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0) == 0) {
            supportFinishAfterTransition();
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
            case R.id.next_button:
                updateFragment(TAG_CUSTOMER_DETAIL, true);
                buttonViewSwitcher.setDisplayedChild(1);
                break;

            case R.id.back_button:
                updateFragment(TAG_SUMMARY, true);
                buttonViewSwitcher.setDisplayedChild(0);
                hideKeyboard();
                break;

            case R.id.place_order_button:
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
                if (sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0) == 0) {
                    Toast.makeText(this, R.string.place_order_no_item_error_message, Toast.LENGTH_LONG).show();
                } else if (isNetworkConnected) {
                    CartCustomerDetailFragment customerDetailFragment
                            = (CartCustomerDetailFragment) getFragmentManager().findFragmentByTag(TAG_CUSTOMER_DETAIL);
                    if (customerDetailFragment == null) {
                        Toast.makeText(this, R.string.place_order_error_message, Toast.LENGTH_LONG).show();
                    } else if (customerDetailFragment.checkAllFields()) {
                        updateFragment(TAG_CONFIRM_ORDER_DIALOG, false);
                        deliverTime = customerDetailFragment.getDeliverTime();
                    }
                } else {
                    Toast.makeText(this, R.string.no_network_place_order_error_message, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onPlaceOrderConfirmed() {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.findInBackground(new FindCallback<OrderItem>() {
            @Override
            public void done(List<OrderItem> orderItemList, ParseException e) {
                if (e != null) {
                    Toast.makeText(CartActivity.this, R.string.place_order_error_message, Toast.LENGTH_LONG).show();
                } else {
                    showPlacingOrderDialog();
                    saveOrder(orderItemList);
                }
            }
        });
    }

    private void updateFragment(String tag, boolean showAnimation) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.cart_content_frame);
        switch (tag) {
            case TAG_SUMMARY:
                if (fragment == null || !(fragment instanceof CartSummaryFragment)) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    if (showAnimation) {
                        fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                    }
                    fragmentTransaction.replace(R.id.cart_content_frame, CartSummaryFragment.newInstance(), tag)
                            .addToBackStack(null)
                            .commit();
                }
                break;

            case TAG_CUSTOMER_DETAIL:
                if (fragment == null || !(fragment instanceof CartCustomerDetailFragment)) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    if (showAnimation) {
                        fragmentTransaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                    fragmentTransaction.replace(R.id.cart_content_frame, CartCustomerDetailFragment.newInstance(), tag)
                            .addToBackStack(tag)
                            .commit();
                }
                break;

            case TAG_CONFIRM_ORDER_DIALOG:
                ConfirmOrderDialogFragment confirmOrderDialogFragment
                        = (ConfirmOrderDialogFragment) getFragmentManager().findFragmentByTag(TAG_CONFIRM_ORDER_DIALOG);
                if (confirmOrderDialogFragment == null) {
                    confirmOrderDialogFragment = ConfirmOrderDialogFragment.newInstance();
                }
                confirmOrderDialogFragment.show(getFragmentManager(), TAG_CONFIRM_ORDER_DIALOG);
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(buttonViewSwitcher.getWindowToken(), 0);
    }

    private void showPlacingOrderDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_PLACING_ORDER_DIALOG);
        if (fragment == null) {
            PlacingOrderDialogFragment.newInstance().show(getFragmentManager(), TAG_PLACING_ORDER_DIALOG);
        }
    }

    private void dismissPlacingOrderDialog() {
        PlacingOrderDialogFragment fragment
                = (PlacingOrderDialogFragment) getFragmentManager().findFragmentByTag(TAG_PLACING_ORDER_DIALOG);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private void saveOrder(List<OrderItem> orderItemList) {
        final Order order = new Order();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd yyyy, h:mm a", Locale.US);
        order.setOrderTime(simpleDateFormat.format(new Date()));
        order.setDeliverBy(simpleDateFormat.format(deliverTime));
        order.setTotalQuantity(sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0));
        double totalPrice = 0;
        List<String> completeOrderList = new ArrayList<>();
        List<String> toppingList;
        StringBuilder stringBuilder;
        OrderItem orderItem;
        for (int i = 0; i < orderItemList.size(); i++) {
            orderItem = orderItemList.get(i);
            totalPrice += orderItem.getTotalPrice();
            stringBuilder = new StringBuilder();
            stringBuilder.append(orderItem.getName())
                    .append(NAME_SPLIT_SYMBOL);
            toppingList = orderItem.getSelectedToppingsList();
            for (int j = 0; j < toppingList.size(); j++) {
                if (j > 0) {
                    stringBuilder.append(TOPPING_SPLIT_SYMBOL);
                }
                stringBuilder.append(toppingList.get(j));
            }
            completeOrderList.add(stringBuilder.toString());
        }
        order.setTotalPrice(totalPrice);
        order.setCompleteOrderList(completeOrderList);
        order.setAnonymousUserId(DataUtils.getDeviceId(CartActivity.this));

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
        if (isNetworkConnected) {
            saveOrderToRemoteAndFinish(order);
        } else {
            dismissPlacingOrderDialog();
            Toast.makeText(this, R.string.no_network_place_order_error_message, Toast.LENGTH_LONG).show();
        }
    }

    private void saveOrderToRemoteAndFinish(final Order order) {
        order.pinInBackground(MojoTeaApp.ORDER_GROUP, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                order.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        dismissPlacingOrderDialog();
                        if (e != null) {
                            Toast.makeText(CartActivity.this, R.string.place_order_error_message, Toast.LENGTH_LONG).show();
                        } else {
                            ParseObject.unpinAllInBackground(MojoTeaApp.ORDER_ITEM_GROUP);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0);
                            editor.putStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, new HashSet<String>());
                            editor.apply();

                            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                            Intent intent = new Intent(CartActivity.this, PostOrderActivity.class);
                            intent.putExtra(PostOrderActivity.EXTRA_TOTAL_PRICE, order.getTotalPrice());
                            intent.putExtra(PostOrderActivity.EXTRA_DELIVER_TIME, simpleTimeFormat.format(deliverTime));
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }
}
