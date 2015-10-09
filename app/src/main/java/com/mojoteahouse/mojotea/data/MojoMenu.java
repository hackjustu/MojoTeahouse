package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParseClassName("MojoMenu")
public class MojoMenu extends ParseObject {

    public static final String MENU_ID = "menuId";
    public static final int DEFAULT_PRIORITY = 0;
    private static final String IMAGE_ID = "imageId";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String TOPPINGS = "toppings";
    private static final String CATEGORY = "category";
    private static final String PRIORITY = "priority";
    private static final String SOLD_OUT = "soldOut";

    public static ParseQuery<MojoMenu> getQuery() {
        return ParseQuery.getQuery(MojoMenu.class);
    }

    public MojoMenu() {

    }

    public int getMenuId() {
        return getInt(MENU_ID);
    }

    public int getImageId() {
        return getInt(IMAGE_ID);
    }

    public String getName() {
        return getString(NAME);
    }

    public double getPrice() {
        return getDouble(PRICE);
    }

    public List<Integer> getToppingIdList() {
        List<Integer> toppingList = getList(TOPPINGS);
        if (toppingList == null) {
            return new ArrayList<>();
        }
        Collections.sort(toppingList);
        return toppingList;
    }

    public String getCategory() {
        return getString(CATEGORY);
    }

    public int getPriority() {
        return getInt(PRIORITY);
    }

    public boolean isSoldOut() {
        return getBoolean(SOLD_OUT);
    }
}