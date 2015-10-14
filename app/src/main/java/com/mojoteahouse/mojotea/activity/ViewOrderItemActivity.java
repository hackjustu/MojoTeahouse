package com.mojoteahouse.mojotea.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.MojoImage;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.data.Topping;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewOrderItemActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ITEM_ID = "EXTRA_ORDER_ITEM_ID";

    private ParseImageView orderItemImageView;
    private TextView orderItemNameTextView;
    private TextView quantityText;
    private TextView noteText;
    private TextView noToppingTextView;
    private List<String> selectedToppings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order_item);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        orderItemImageView = (ParseImageView) findViewById(R.id.mojo_item_image);
        orderItemNameTextView = (TextView) findViewById(R.id.mojo_item_name_text);
        quantityText = (TextView) findViewById(R.id.quantity_text);
        noteText = (TextView) findViewById(R.id.note_text);
        noToppingTextView = (TextView) findViewById(R.id.no_topping_text);

        loadOrderItemInBackground(getIntent().getStringExtra(EXTRA_ORDER_ITEM_ID));
    }

    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
    }

    private void loadOrderItemInBackground(String orderItemId) {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.whereEqualTo(OrderItem.ORDER_ITEM_ID, orderItemId);
        orderItemQuery.getFirstInBackground(new GetCallback<OrderItem>() {
            @Override
            public void done(OrderItem orderItem, ParseException e) {
                if (e != null) {
                    Toast.makeText(ViewOrderItemActivity.this, R.string.get_cart_item_error_message, Toast.LENGTH_LONG).show();
                    supportFinishAfterTransition();
                } else {
                    selectedToppings = orderItem.getSelectedToppingsList();
                    loadToppingListInBackground(orderItem.getAssociatedMojoMenu());
                    int imageId = orderItem.getAssociatedMojoMenu().getImageId();
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
                    orderItemNameTextView.setText(orderItem.getName());
                    orderItemNameTextView.setVisibility(View.VISIBLE);
                    quantityText.setText(String.valueOf(orderItem.getQuantity()));
                    noteText.setText(orderItem.getNote());
                }
            }
        });
    }

    private void loadToppingListInBackground(MojoMenu mojoMenu) {
        final List<Integer> availableToppingIdList = mojoMenu.getToppingIdList();
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
                    Toast.makeText(ViewOrderItemActivity.this, R.string.get_topping_error_message, Toast.LENGTH_LONG).show();
                } else if (toppings != null) {
                    for (Topping topping : toppings) {
                        if (availableToppingIdList.contains(topping.getToppingId())) {
                            toppingList.add(topping);
                        }
                    }
                    if (!toppingList.isEmpty()) {
                        addToppings(toppingList);
                        noToppingTextView.setVisibility(View.GONE);
                    } else {
                        noToppingTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    noToppingTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void addToppings(List<Topping> toppingList) {
        Collections.sort(toppingList, new ToppingComparator());
        LinearLayout toppingContainer = (LinearLayout) findViewById(R.id.topping_container);
        toppingContainer.removeAllViews();
        for (final Topping topping : toppingList) {
            final CheckedTextView checkedTextView = (CheckedTextView) getLayoutInflater().inflate(
                    R.layout.topping_list_item, toppingContainer, false);
            checkedTextView.setText(String.format(getString(R.string.topping_format),
                    topping.getName(), topping.getPrice()));
            checkedTextView.setTag(topping);
            checkedTextView.setChecked(selectedToppings.contains(topping.getName()));
            checkedTextView.setEnabled(false);
            toppingContainer.addView(checkedTextView);
        }
    }


    private class ToppingComparator implements Comparator<Topping> {

        @Override
        public int compare(Topping first, Topping second) {
            if (first.getName() == null) {
                return 1;
            }
            return first.getName().compareTo(second.getName());
        }
    }
}
