package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

@ParseClassName("MojoMenu")
public class MojoMenu extends ParseObject {

    public static final String MENU_ID = "menuId";
    private static final String IMAGE = "image";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String TOPPINGS = "toppings";
    private static final String PRIORITY = "priority";

    public static ParseQuery<MojoMenu> getQuery() {
        return ParseQuery.getQuery(MojoMenu.class);
    }

    public MojoMenu() {

    }

    public int getMenuId() {
        return getInt(MENU_ID);
    }

    public ParseFile getImage() {
        return getParseFile(IMAGE);
    }

    public String getName() {
        return getString(NAME);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public List<Integer> getToppingList() {
        return getList(TOPPINGS);
    }

    public int getPriority() {
        return getInt(PRIORITY);
    }
}