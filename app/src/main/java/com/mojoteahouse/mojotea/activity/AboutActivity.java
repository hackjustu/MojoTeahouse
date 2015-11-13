package com.mojoteahouse.mojotea.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout addressSection = (RelativeLayout) findViewById(R.id.address_secion);
        addressSection.setOnLongClickListener(this);

        RelativeLayout phoneSection = (RelativeLayout) findViewById(R.id.phone_secion);
        phoneSection.setOnClickListener(this);

        RelativeLayout emailSection = (RelativeLayout) findViewById(R.id.email_section);
        emailSection.setOnClickListener(this);

        RelativeLayout openHoursSection = (RelativeLayout) findViewById(R.id.open_hours_section);
        openHoursSection.setOnClickListener(this);
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
            case R.id.address_secion:
                break;

            case R.id.phone_secion:
                showDialingConfirmDialog();
                break;

            case R.id.email_section:
                sendEmail();
                break;

            case R.id.open_hours_section:
                Toast.makeText(AboutActivity.this, "药药~切克闹~~", Toast.LENGTH_SHORT).show();
                break;

            default:
        }
    }

    private void showDialingConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
        builder.setMessage("Call Mojo Teahouse?")
                .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callPhone();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and show it
        builder.create().show();
    }

    private void callPhone() {

        String phoneNumber = "18051234567";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(callIntent);
    }

    private void sendEmail() {

        String[] TO = {"contacts@example.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(AboutActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.address_secion:
                copyToClipBoard();
                break;

            case R.id.phone_secion:
                break;

            case R.id.email_section:
                break;

            case R.id.open_hours_section:
                break;

            default:
        }
        return true;
    }

    private void copyToClipBoard() {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(
                "text label",
                "1234 Hollister Ave, Goleta, CA 93111");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(AboutActivity.this, "Saved to clip board", Toast.LENGTH_SHORT).show();
    }
}
