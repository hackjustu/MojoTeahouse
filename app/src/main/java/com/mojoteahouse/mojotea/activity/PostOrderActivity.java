package com.mojoteahouse.mojotea.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;

public class PostOrderActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_TOTAL_PRICE = "EXTRA_TOTAL_PRICE";
    public static final String EXTRA_DELIVER_TIME = "EXTRA_DELIVER_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_order);

        TextView priceText = (TextView) findViewById(R.id.price_text);
        TextView deliverTimeText = (TextView) findViewById(R.id.deliver_time_text);
        TextView contactText = (TextView) findViewById(R.id.contact_text);
        ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
        Button gotItButton = (Button) findViewById(R.id.got_it_button);

        Intent launchIntent = getIntent();
        priceText.setText(String.format(getString(R.string.order_price_format),
                launchIntent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0)));
        deliverTimeText.setText(String.format(getString(R.string.order_deliver_time_format),
                launchIntent.getStringExtra(EXTRA_DELIVER_TIME)));

        contactText.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        gotItButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_text:

                break;

            case R.id.got_it_button:
            case R.id.close_button:
                finishAndShowOrderPage();
                break;
        }
    }

    private void finishAndShowOrderPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SHOW_ORDER_PAGE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
