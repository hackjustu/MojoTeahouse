package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

@ParseClassName("Order")
public class Order extends ParseObject {

    public static final String ANONYMOUS_USER_ID = "anonymousUserId";
    private static final String SUMMARY = "summary";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String STATUS = "status";
    private static final String ORDER_ITEM_LIST = "ORDER_ITEM_LIST";

    public static ParseQuery<Order> getQuery() {
        return ParseQuery.getQuery(Order.class);
    }

    public Order() {

    }

    public Date getOrderTime() {
        return getCreatedAt();
    }

    public void setAnonymousUserId(String anonymousUserId) {
        put(ANONYMOUS_USER_ID, anonymousUserId);
    }

    public String getSummary() {
        return getString(SUMMARY);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public String getStatus() {
        return getString(STATUS);
    }

    public List<OrderItem> getOrderItemList() {
        return getList(ORDER_ITEM_LIST);
    }
}
