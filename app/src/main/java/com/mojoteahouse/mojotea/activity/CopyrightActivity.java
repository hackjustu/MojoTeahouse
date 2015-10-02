package com.mojoteahouse.mojotea.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CopyrightActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copyright);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        TextView copyright = (TextView) findViewById(R.id.copyright_text);
        copyright.setText(getCopyrightText());
    }

    private CharSequence getCopyrightText() {
        InputStream inputStream = getResources().openRawResource(R.raw.copyright);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }

        String copyright = text.toString();
        return Html.fromHtml(copyright);
    }
}
