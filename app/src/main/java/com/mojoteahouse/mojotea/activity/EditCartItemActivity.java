package com.mojoteahouse.mojotea.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.ToppingItemAdapter;
import com.mojoteahouse.mojotea.data.MojoImage;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.data.Topping;
import com.mojoteahouse.mojotea.fragment.DeleteCartItemDialogFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditCartItemActivity extends AppCompatActivity implements View.OnClickListener,
        ToppingItemAdapter.ToppingItemClickListener, DeleteCartItemDialogFragment.DeleteCartItemListener {

    public static final String EXTRA_ORDER_ITEM_ID = "EXTRA_ORDER_ITEM_ID";
    public static final String EXTRA_SELECTED_TOPPINGS = "EXTRA_SELECTED_TOPPINGS";
    public static final String EXTRA_QUANTITY = "EXTRA_QUANTITY";

    private static final String TAG_DELETE_DIALOG = "TAG_DELETE_DIALOG";
    private static final String SPLIT_SYMBOL = "%";
    private static final int DISPLAY_QUANTITY_EDIT_TEXT = 1;
    private static final int SPINNER_MAX_VALUE = 5;

    private ParseImageView orderItemImageView;
    private TextView orderItemNameTextView;
    private Button editDoneButton;
    private RecyclerView toppingsRecyclerView;
    private ToppingItemAdapter toppingItemAdapter;
    private TextView noToppingTextView;
    private SharedPreferences sharedPreferences;
    private String orderItemId;
    private OrderItem localOrderItem;
    private MojoMenu localMojoMenu;
    private List<String> selectedToppings;
    private int quantity;
    private double totalPrice;
    private String orderItemName;
    private double mojoItemPrice;
    private double toppingPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cart_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent launchIntent = getIntent();
        orderItemId = launchIntent.getStringExtra(EXTRA_ORDER_ITEM_ID);
        quantity = launchIntent.getIntExtra(EXTRA_QUANTITY, 0);
        selectedToppings = launchIntent.getStringArrayListExtra(EXTRA_SELECTED_TOPPINGS);

        orderItemImageView = (ParseImageView) findViewById(R.id.order_item_image);
        orderItemNameTextView = (TextView) findViewById(R.id.order_item_name_text);
        editDoneButton = (Button) findViewById(R.id.edit_done_button);
        editDoneButton.setOnClickListener(this);

        final ViewSwitcher quantityViewSwitcher = (ViewSwitcher) findViewById(R.id.quantity_view_switcher);
        final EditText quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        if (quantity > SPINNER_MAX_VALUE) {
            quantityEditText.setText(String.valueOf(quantity));
            quantityViewSwitcher.setDisplayedChild(DISPLAY_QUANTITY_EDIT_TEXT);
        }
        quantityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    if (TextUtils.isEmpty(quantityEditText.getText())) {
                        quantity = 1;
                        quantityEditText.setText(R.string.quantity_initial_value);
                    } else {
                        quantity = Integer.parseInt(quantityEditText.getText().toString());
                    }
                    if (quantity < 1) {
                        quantity = 1;
                    }
                    updatePriceAndText();
                }
                return false;
            }
        });
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    quantity = 1;
                } else {
                    quantity = Integer.parseInt(s.toString());
                }
                if (quantity < 1) {
                    quantity = 1;
                }
                updatePriceAndText();
            }
        });
        Spinner quantitySpinner = (Spinner) findViewById(R.id.quantity_spinner);
        quantitySpinner.setSelection(quantity > SPINNER_MAX_VALUE ? 0 : quantity - 1);
        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (getString(R.string.quantity_customize_text).equals(selectedItem)) {
                    quantityViewSwitcher.setDisplayedChild(DISPLAY_QUANTITY_EDIT_TEXT);
                    quantityEditText.setText("");
                    quantityEditText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(quantityEditText, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    quantity = Integer.parseInt(selectedItem);
                    updatePriceAndText();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toppingsRecyclerView = (RecyclerView) findViewById(R.id.toppings_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        toppingsRecyclerView.setLayoutManager(linearLayoutManager);
        toppingItemAdapter = new ToppingItemAdapter(this, new ArrayList<Topping>(), this);
        toppingsRecyclerView.setAdapter(toppingItemAdapter);
        noToppingTextView = (TextView) findViewById(R.id.no_topping_text);

        loadOrderItemInBackground(orderItemId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_summary_action_mode_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelAndFinish();
                break;

            case R.id.action_delete:
                DeleteCartItemDialogFragment fragment = (DeleteCartItemDialogFragment) getFragmentManager().findFragmentByTag(TAG_DELETE_DIALOG);
                if (fragment == null) {
                    fragment = DeleteCartItemDialogFragment.newInstance();
                    fragment.show(getFragmentManager(), TAG_DELETE_DIALOG);
                }
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_done_button:
                saveOrderItemAndFinish();
                break;
        }
    }

    @Override
    public void onToppingItemClicked(double toppingPrice) {
        this.toppingPrice += toppingPrice;
        if (this.toppingPrice < 0) {
            this.toppingPrice = 0;
        }
        updatePriceAndText();
    }

    @Override
    public void onDeleteConfirmed() {
        final Set<String> orderItemIdSet = sharedPreferences.getStringSet(
                MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, new HashSet<String>());
        int totalOrderItemCount = sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0);
        final int newOrderItemCount = totalOrderItemCount - localOrderItem.getQuantity();
        for (final String orderItemString : orderItemIdSet) {
            if (orderItemId.equals(orderItemString.split(SPLIT_SYMBOL)[0])) {
                localOrderItem.unpinInBackground(MojoTeaApp.ORDER_ITEM_GROUP, new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(EditCartItemActivity.this, R.string.delete_failed_error_message, Toast.LENGTH_LONG).show();
                        } else {
                            orderItemIdSet.remove(orderItemString);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, orderItemIdSet);
                            editor.putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, newOrderItemCount);
                            editor.apply();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                });
                break;
            }
        }
    }

    private void loadOrderItemInBackground(String orderItemId) {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.whereEqualTo(OrderItem.ORDER_ITEM_ID, orderItemId);
        orderItemQuery.getFirstInBackground(new GetCallback<OrderItem>() {
            @Override
            public void done(OrderItem orderItem, ParseException e) {
                if (e != null) {
                    Toast.makeText(EditCartItemActivity.this, R.string.get_cart_item_error_message, Toast.LENGTH_LONG).show();
                    cancelAndFinish();
                } else {
                    localOrderItem = orderItem;
                    localMojoMenu = orderItem.getAssociatedMojoMenu();
                    loadToppingListInBackground();
                    int imageId = localMojoMenu.getImageId();
                    ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
                    mojoImageQuery.fromLocalDatastore();
                    mojoImageQuery.whereEqualTo(MojoImage.IMAGE_ID, imageId);
                    mojoImageQuery.getFirstInBackground(new GetCallback<MojoImage>() {
                        @Override
                        public void done(MojoImage mojoImage, ParseException e) {
                            if (e == null) {
                                orderItemImageView.setParseFile(mojoImage.getImage());
                                orderItemImageView.loadInBackground();
                            }
                        }
                    });
                    orderItemName = orderItem.getName();
                    orderItemNameTextView.setText(orderItemName);
                    orderItemNameTextView.setVisibility(View.VISIBLE);
                    mojoItemPrice = localMojoMenu.getPrice();
                    toppingPrice = orderItem.getSelectedToppingPrice();
                    updatePriceAndText();
                    editDoneButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadToppingListInBackground() {
        final List<Integer> availableToppingIdList = localMojoMenu.getToppingIdList();
        if (availableToppingIdList == null || availableToppingIdList.size() == 0) {
            noToppingTextView.setVisibility(View.VISIBLE);
            return;
        }
        final List<Topping> toppingList = new ArrayList<>();
        ParseQuery<Topping> toppingQuery = Topping.getQuery();
        toppingQuery.fromLocalDatastore();
        toppingQuery.findInBackground(new FindCallback<Topping>() {
            @Override
            public void done(List<Topping> toppings, ParseException e) {
                if (e != null) {
                    Toast.makeText(EditCartItemActivity.this, R.string.get_topping_error_message, Toast.LENGTH_LONG).show();
                } else if (toppings != null) {
                    for (Topping topping : toppings) {
                        if (availableToppingIdList.contains(topping.getToppingId())) {
                            toppingList.add(topping);
                        }
                    }
                    if (!toppingList.isEmpty()) {
                        toppingItemAdapter.updateWithSelectedToppingList(toppingList, selectedToppings);
                        noToppingTextView.setVisibility(View.GONE);
                        toppingsRecyclerView.setVisibility(View.VISIBLE);
                        toppingItemAdapter.notifyDataSetChanged();
                    } else {
                        noToppingTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    noToppingTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updatePriceAndText() {
        totalPrice = quantity * (mojoItemPrice + toppingPrice);
        editDoneButton.setText(String.format(getString(R.string.edit_done_button_text), totalPrice));
    }

    private void saveOrderItemAndFinish() {
        List<String> selectedToppings = toppingItemAdapter.getSelectedToppingList();
        Collections.sort(selectedToppings);
        int totalCount = sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0);
        final int newCount = totalCount - localOrderItem.getQuantity() + quantity;
        localOrderItem.setQuantity(quantity);
        localOrderItem.setSelectedToppingPrice(toppingPrice);
        localOrderItem.setSelectedToppingsList(selectedToppings);
        localOrderItem.setTotalPrice(totalPrice);
        localOrderItem.pinInBackground(MojoTeaApp.ORDER_ITEM_GROUP, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(EditCartItemActivity.this, R.string.edit_cart_item_error_message, Toast.LENGTH_LONG).show();
                    cancelAndFinish();
                } else {
                    sharedPreferences.edit().putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, newCount).apply();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    private void cancelAndFinish() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
