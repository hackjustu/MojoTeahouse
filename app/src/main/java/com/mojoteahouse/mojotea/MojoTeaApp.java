package com.mojoteahouse.mojotea;

import android.app.Application;

import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.Order;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class MojoTeaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Add data classes
        ParseObject.registerSubclass(MojoMenu.class);
        ParseObject.registerSubclass(Order.class);

        // Initialize and setup Parse
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
