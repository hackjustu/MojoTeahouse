package com.mojoteahouse.mojotea;

import android.app.Application;

import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.data.Topping;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MojoTeaApp extends Application {

    public static final String MOJO_MENU_GROUP = "MOJO_MENU_GROUP";
    public static final String ORDER_HISTORY_GROUP = "ORDER_HISTORY_GROUP";
    public static final String ORDER_ITEM_GROUP = "ORDER_ITEM_GROUP";
    public static final String CART_DETAILS_GROUP = "CART_DETAILS_GROUP";
    public static final String TOPPING_GROUP = "TOPPING_GROUP";

    public static final String PREF_EXISTING_ORDER = "PREF_EXISTING_ORDER";

    @Override
    public void onCreate() {
        super.onCreate();

        // Add data classes
        ParseObject.registerSubclass(MojoMenu.class);
        ParseObject.registerSubclass(Order.class);
        ParseObject.registerSubclass(Topping.class);
        ParseObject.registerSubclass(OrderItem.class);

        // Initialize and setup Parse
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        ParseACL.setDefaultACL(defaultACL, true);
    }
}