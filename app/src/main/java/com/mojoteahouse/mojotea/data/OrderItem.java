package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

@ParseClassName("OrderItem")
public class OrderItem extends ParseObject {

    public static final String ORDER_ITEM_ID = "orderItemId";
    private static final String IMAGE = "image";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String SELECTED_TOPPINGS = "selectedToppings";

    public static ParseQuery<OrderItem> getQuery() {
        return ParseQuery.getQuery(OrderItem.class);
    }

    public OrderItem() {

    }

    public long getOrderItemId() {
        return getLong(ORDER_ITEM_ID);
    }

    public void setOrderItemId(long orderItemId) {
        put(ORDER_ITEM_ID, orderItemId);
    }

    public ParseFile getImage() {
        return getParseFile(IMAGE);
    }

    public void setImage(ParseFile image) {
        put(IMAGE, image);
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

    public List<String> getSelectedToppingsList() {
        return getList(SELECTED_TOPPINGS);
    }

    public void setSelectedToppingsList(List<String> selectedToppingsList) {
        put(SELECTED_TOPPINGS, selectedToppingsList);
    }
}