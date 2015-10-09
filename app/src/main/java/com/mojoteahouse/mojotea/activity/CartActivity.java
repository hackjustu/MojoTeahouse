package com.mojoteahouse.mojotea.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ViewSwitcher;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.fragment.CartCustomerDetailFragment;
import com.mojoteahouse.mojotea.fragment.CartSummaryFragment;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG_SUMMARY = "TAG_SUMMARY";
    private static final String TAG_CUSTOMER_DETAIL = "TAG_CUSTOMER_DETAIL";

    private ViewSwitcher buttonViewSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.cart_toolbar_title);

        buttonViewSwitcher = (ViewSwitcher) findViewById(R.id.button_view_switcher);
        Button nextButton = (Button) findViewById(R.id.next_button);
        Button backButton = (Button) findViewById(R.id.back_button);
        Button placeOrderButton = (Button) findViewById(R.id.place_order_button);
        nextButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        placeOrderButton.setOnClickListener(this);

        updateFragment(TAG_SUMMARY, false);
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
                break;

            case R.id.place_order_button:

                break;
        }
    }

    private void updateFragment(String tag, boolean showAnimation) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
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
        }
    }
}
