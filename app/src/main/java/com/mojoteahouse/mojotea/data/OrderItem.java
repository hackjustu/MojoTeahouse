package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

@ParseClassName("OrderItem")
public class OrderItem extends ParseObject {

    public static final String ORDER_ITEM_ID = "orderItemId";
    public static final String ORDER_PLACED = "orderPlaced";
    private static final String ASSOCIATED_MOJO_MENU = "associatedMojoMenu";
    private static final String NAME = "name";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String QUANTITY = "quantity";
    private static final String SELECTED_TOPPINGS = "selectedToppings";
    private static final String SELECTED_TOPPING_PRICE = "selectedToppingPrice";
    private static final String NOTE = "note";

    public static ParseQuery<OrderItem> getQuery() {
        return ParseQuery.getQuery(OrderItem.class);
    }

    public OrderItem() {

    }

    public String getOrderItemId() {
        return getString(ORDER_ITEM_ID);
    }

    public void setOrderItemId(String orderItemId) {
        put(ORDER_ITEM_ID, orderItemId);
    }

    public MojoMenu getAssociatedMojoMenu() {
        return (MojoMenu) getParseObject(ASSOCIATED_MOJO_MENU);
    }

    public void setAssociatedMojoMenu(MojoMenu mojoMenu) {
        put(ASSOCIATED_MOJO_MENU, mojoMenu);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String name) {
        put(NAME, name);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public void setTotalPrice(double price) {
        put(TOTAL_PRICE, price);
    }

    public int getQuantity() {
        return getInt(QUANTITY);
    }

    public void setQuantity(int quantity) {
        put(QUANTITY, quantity);
    }

    public List<String> getSelectedToppingsList() {
        return getList(SELECTED_TOPPINGS);
    }

    public void setSelectedToppingsList(List<String> selectedToppingsList) {
        put(SELECTED_TOPPINGS, selectedToppingsList);
    }

    public double getSelectedToppingPrice() {
        return getDouble(SELECTED_TOPPING_PRICE);
    }

    public void setSelectedToppingPrice(double price) {
        put(SELECTED_TOPPING_PRICE, price);
    }

    public boolean isOrderPlaced() {
        return getBoolean(ORDER_PLACED);
    }

    public void setOrderPlaced(boolean orderPlaced) {
        put(ORDER_PLACED, orderPlaced);
    }

    public String getNote() {
        return getString(NOTE);
    }

    public void setNote(String note) {
        put(NOTE, note);
    }
}