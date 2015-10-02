package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Topping")
public class Topping extends ParseObject {

    public static final String TOPPING_ID = "toppingId";
    private static final String NAME = "name";
    private static final String PRICE = "price";

    public static ParseQuery<Topping> getQuery() {
        return ParseQuery.getQuery(Topping.class);
    }

    public Topping() {

    }

    public int getToppingId() {
        return getInt(TOPPING_ID);
    }

    public String getName() {
        return getString(NAME);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }
}