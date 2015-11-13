package com.mojoteahouse.mojotea.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;

import org.apache.http.impl.io.AbstractSessionOutputBuffer;

public class PostOrderActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_CUSTOMER_NAME = "EXTRA_CUSTOMER_NAME";
    public static final String EXTRA_TOTAL_PRICE = "EXTRA_TOTAL_PRICE";
    public static final String EXTRA_DELIVER_TIME = "EXTRA_DELIVER_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_order);

        TextView nameText = (TextView) findViewById(R.id.greeting_name);
        TextView priceText = (TextView) findViewById(R.id.price_text);
        TextView deliverTimeText = (TextView) findViewById(R.id.deliver_time_text);
        Button contactButton = (Button) findViewById(R.id.contact_button);
        ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
        Button gotItButton = (Button) findViewById(R.id.got_it_button);

        Intent launchIntent = getIntent();
        nameText.setText(launchIntent.getStringExtra(EXTRA_CUSTOMER_NAME)+",");
        priceText.setText(String.format(getString(R.string.order_price_format),
                launchIntent.getDoubleExtra(EXTRA_TOTAL_PRICE, 0)));
        deliverTimeText.setText(String.format(getString(R.string.order_deliver_time_format),
                launchIntent.getStringExtra(EXTRA_DELIVER_TIME)));

        contactButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        gotItButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contact_button:
                finishAndShowAboutPage();
                break;
            case R.id.got_it_button:
                finishAndShowOrderPage();
                break;
            case R.id.close_button:
                finishAndShowOrderPage();
                break;
        }
    }

    private void finishAndShowAboutPage() {
        Intent intent = new Intent(PostOrderActivity.this, AboutActivity.class);
        startActivity(intent);
        finish();
    }

    private void finishAndShowOrderPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_SHOW_ORDER_PAGE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
