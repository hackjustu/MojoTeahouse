package com.mojoteahouse.mojotea.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.ToppingItemAdapter;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.data.Topping;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class EditMojoItemActivity extends AppCompatActivity implements View.OnClickListener, ToppingItemAdapter.ToppingItemClickListener {

    public static final String EXTRA_MOJO_MENU_NAME = "EXTRA_MOJO_MENU_NAME";
    public static final String EXTRA_MOJO_MENU_PRICE = "EXTRA_MOJO_MENU_PRICE";
    public static final String EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS = "EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS";
    public static final String EXTRA_QUANTITY = "EXTRA_QUANTITY";
    public static final String EXTRA_ORDER_ITEM_ID = "EXTRA_ORDER_ITEM_ID";

    private Button addToCartButton;
    private TextInputLayout quantityTextInputLayout;
    private EditText quantityEditText;
    private RecyclerView toppingsRecyclerView;
    private ToppingItemAdapter toppingItemAdapter;
    private TextView noToppingTextView;
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
        mojoItemName = launchIntent.getStringExtra(EXTRA_MOJO_MENU_NAME);
        mojoItemPrice = launchIntent.getDoubleExtra(EXTRA_MOJO_MENU_PRICE, 0);
        totalPrice = mojoItemPrice;
        quantity = 1;
        toppingPrice = 0;

        TextView mojoItemNameTextView = (TextView) findViewById(R.id.mojo_item_name_text);
        mojoItemNameTextView.setText(mojoItemName);

        addToCartButton = (Button) findViewById(R.id.add_to_cart_button);
        addToCartButton.setText(String.format(getString(R.string.add_to_cart_button_text), mojoItemPrice));
        addToCartButton.setOnClickListener(this);

        quantityTextInputLayout = (TextInputLayout) findViewById(R.id.quantity_text_input_layout);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String number = s.toString();
                if (TextUtils.isEmpty(number)) {
                    quantity = 0;
                } else {
                    try {
                        quantity = Integer.parseInt(number);
                    } catch (Exception e) {
                        quantity = 0;
                    }
                }
                updatePriceAndText();
            }
        });

        toppingsRecyclerView = (RecyclerView) findViewById(R.id.toppings_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        toppingsRecyclerView.setLayoutManager(linearLayoutManager);
        toppingItemAdapter = new ToppingItemAdapter(EditMojoItemActivity.this, new ArrayList<Topping>(), this);
        toppingsRecyclerView.setAdapter(toppingItemAdapter);
        noToppingTextView = (TextView) findViewById(R.id.no_topping_text);

        loadToppingListInBackground(launchIntent.getIntegerArrayListExtra(EXTRA_MOJO_MENU_AVAILABLE_TOPPINGS));
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
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
        final int quantity = Integer.parseInt(quantityEditText.getText().toString());
        if (quantity <= 0) {
            quantityTextInputLayout.setError(getString(R.string.quantity_error_message));
            return;
        }
        final OrderItem orderItem = new OrderItem();
        orderItem.setName(mojoItemName);
        orderItem.setPrice(totalPrice);
        orderItem.setSelectedToppingsList(toppingItemAdapter.getSelectedToppingList());
        final long orderItemId = System.currentTimeMillis();
        orderItem.setOrderItemId(orderItemId);
        orderItem.pinInBackground(MojoTeaApp.ORDER_ITEM_GROUP, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(EditMojoItemActivity.this, R.string.add_to_cart_error_message, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_QUANTITY, quantity);
                    data.putExtra(EXTRA_ORDER_ITEM_ID, orderItemId);
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });
    }
}
