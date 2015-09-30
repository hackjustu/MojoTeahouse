package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("MojoMenu")
public class MojoMenu extends ParseObject {

    private static final String IMAGE = "image";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String PRIORITY = "priority";

    public MojoMenu() {

    }

    public static ParseQuery<MojoMenu> getQuery() {
        return ParseQuery.getQuery(MojoMenu.class);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public void setPrice(double price) {
        put(PRICE, price);
    }

    public int getPriority() {
        return getInt(PRIORITY);
    }

    public void setPriority(int priority) {
        put(PRIORITY, priority);
    }
}