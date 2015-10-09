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

public class EditMojoItemActivity extends AppCompatActivity implements View.OnClickListener, ToppingItemAdapter.ToppingItemClickListener {

    public static final String EXTRA_MOJO_MENU_ID = "EXTRA_MOJO_MENU_ID";
    public static final String EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS = "EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS";
    public static final String EXTRA_QUANTITY = "EXTRA_QUANTITY";

    private static final String SPLIT_SYMBOL = "%";
    private static final int DISPLAY_QUANTITY_EDIT_TEXT = 1;

    private ParseImageView mojoItemImageView;
    private TextView mojoItemNameTextView;
    private Button addToCartButton;
    private RecyclerView toppingsRecyclerView;
    private ToppingItemAdapter toppingItemAdapter;
    private TextView noToppingTextView;
    private MojoMenu localMojoMenu;
    private int quantity;
    private double totalPrice;
    private String mojoItemName;
    private double mojoItemPrice;
    private double toppingPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mojo_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent launchIntent = getIntent();
        quantity = 1;
        toppingPrice = 0;

        mojoItemImageView = (ParseImageView) findViewById(R.id.mojo_item_image);
        mojoItemNameTextView = (TextView) findViewById(R.id.mojo_item_name_text);
        addToCartButton = (Button) findViewById(R.id.add_to_cart_button);
        addToCartButton.setOnClickListener(this);

        final ViewSwitcher quantityViewSwitcher = (ViewSwitcher) findViewById(R.id.quantity_view_switcher);
        final EditText quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
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
        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (getString(R.string.quantity_customize_text).equals(selectedItem)) {
                    quantityViewSwitcher.setDisplayedChild(DISPLAY_QUANTITY_EDIT_TEXT);
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
        toppingItemAdapter = new ToppingItemAdapter(EditMojoItemActivity.this, new ArrayList<Topping>(), this);
        toppingsRecyclerView.setAdapter(toppingItemAdapter);
        noToppingTextView = (TextView) findViewById(R.id.no_topping_text);

        loadMojoMenuInBackground(launchIntent.getIntExtra(EXTRA_MOJO_MENU_ID, 0));
        loadToppingListInBackground(launchIntent.getIntegerArrayListExtra(EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS));
    }

    @Override
    public boolean onSupportNavigateUp() {
        cancelAndFinish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_to_cart_button:
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

    private void loadMojoMenuInBackground(int mojoMenuId) {
        ParseQuery<MojoMenu> mojoMenuQuery = MojoMenu.getQuery();
        mojoMenuQuery.fromLocalDatastore();
        mojoMenuQuery.whereEqualTo(MojoMenu.MENU_ID, mojoMenuId);
        mojoMenuQuery.getFirstInBackground(new GetCallback<MojoMenu>() {
            @Override
            public void done(MojoMenu mojoMenu, ParseException e) {
                if (e != null) {
                    Toast.makeText(EditMojoItemActivity.this, R.string.get_mojo_menu_error_message, Toast.LENGTH_LONG).show();
                    cancelAndFinish();
                } else {
                    localMojoMenu = mojoMenu;
                    int imageId = mojoMenu.getImageId();
                    ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
                    mojoImageQuery.fromLocalDatastore();
                    mojoImageQuery.whereEqualTo(MojoImage.IMAGE_ID, imageId);
                    mojoImageQuery.getFirstInBackground(new GetCallback<MojoImage>() {
                        @Override
                        public void done(MojoImage mojoImage, ParseException e) {
                            if (e == null) {
                                mojoItemImageView.setParseFile(mojoImage.getImage());
                                mojoItemImageView.loadInBackground();
                            }
                        }
                    });
                    mojoItemName = mojoMenu.getName();
                    mojoItemNameTextView.setText(mojoItemName);
                    mojoItemNameTextView.setVisibility(View.VISIBLE);
                    mojoItemPrice = mojoMenu.getPrice();
                    updatePriceAndText();
                    addToCartButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadToppingListInBackground(final List<Integer> availableToppingIdList) {
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
                    Toast.makeText(EditMojoItemActivity.this, R.string.get_topping_error_message, Toast.LENGTH_LONG).show();
                } else if (toppings != null) {
                    for (Topping topping : toppings) {
                        if (availableToppingIdList.contains(topping.getToppingId())) {
                            toppingList.add(topping);
                        }
                    }
                    if (!toppingList.isEmpty()) {
                        toppingItemAdapter.updateToppingList(toppingList);
                        noToppingTextView.setVisibility(View.GONE);
                        toppingsRecyclerView.setVisibility(View.VISIBLE);
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
        addToCartButton.setText(String.format(getString(R.string.add_to_cart_button_text), totalPrice));
    }

    private void saveOrderItemAndFinish() {
        List<String> selectedToppings = toppingItemAdapter.getSelectedToppingList();
        Collections.sort(selectedToppings);
        final String orderItemDetail = getOrderItemContentString(selectedToppings);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final Set<String> orderItemIdSet = sharedPreferences.getStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, new HashSet<String>());
        String savedSameOrderItemId = null;

        // Each OrderItemString will be saved as "orderItemId + % + mojoMenuName + all toppings
        // We will use the part after % to check if there is already same item saved.
        for (String str : orderItemIdSet) {
            int splitSymbolPosition = str.indexOf(SPLIT_SYMBOL);
            if (orderItemDetail.equals(str.substring(splitSymbolPosition + 1))) {
                savedSameOrderItemId = str.substring(0, splitSymbolPosition);
            }
        }

        // savedSameOrderItemId will be null if there is no same orderItem in the preferences
        // which means this is a new orderItem, we should save it
        if (savedSameOrderItemId == null) {
            final OrderItem orderItem = new OrderItem();
            orderItem.setAssociatedMojoMenu(localMojoMenu);
            orderItem.setName(mojoItemName);
            orderItem.setTotalPrice(totalPrice);
            orderItem.setSelectedToppingPrice(toppingPrice);
            orderItem.setQuantity(quantity);
            orderItem.setSelectedToppingsList(selectedToppings);
            final String orderItemId = String.valueOf(System.currentTimeMillis());
            orderItem.setOrderItemId(orderItemId);

            orderItem.pinInBackground(MojoTeaApp.ORDER_ITEM_GROUP, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(EditMojoItemActivity.this, R.string.add_to_cart_error_message, Toast.LENGTH_LONG).show();
                        cancelAndFinish();
                    } else {
                        String orderItemString = orderItemId + SPLIT_SYMBOL + orderItemDetail;
                        orderItemIdSet.add(orderItemString);
                        sharedPreferences.edit().putStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, orderItemIdSet).apply();

                        Intent data = new Intent();
                        data.putExtra(EXTRA_QUANTITY, quantity);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
            });
        } else {
            ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
            orderItemQuery.fromLocalDatastore();
            orderItemQuery.whereEqualTo(OrderItem.ORDER_ITEM_ID, savedSameOrderItemId);
            orderItemQuery.getFirstInBackground(new GetCallback<OrderItem>() {
                @Override
                public void done(final OrderItem orderItem, ParseException e) {
                    if (e != null) {
                        Toast.makeText(EditMojoItemActivity.this, R.string.add_to_cart_error_message, Toast.LENGTH_LONG).show();
                        cancelAndFinish();
                    } else {
                        orderItem.setQuantity(orderItem.getQuantity() + quantity);
                        orderItem.pinInBackground(MojoTeaApp.ORDER_ITEM_GROUP, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Toast.makeText(EditMojoItemActivity.this, R.string.add_to_cart_error_message, Toast.LENGTH_LONG).show();
                                    cancelAndFinish();
                                } else {
                                    Intent data = new Intent();
                                    data.putExtra(EXTRA_QUANTITY, quantity);
                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private String getOrderItemContentString(List<String> selectedToppings) {
        StringBuilder sb = new StringBuilder();
        sb.append(mojoItemName);
        for (String topping : selectedToppings) {
            sb.append(topping);
        }
        return sb.toString();
    }

    private void cancelAndFinish() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
