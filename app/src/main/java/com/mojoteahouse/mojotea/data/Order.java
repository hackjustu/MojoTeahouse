package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

@ParseClassName("Order")
public class Order extends ParseObject {

    public static final String ANONYMOUS_USER_ID = "anonymousUserId";
    public static final String ORDER_ID = "orderId";
    private static final String SUMMARY = "summary";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String STATUS = "status";

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

    public void setSummary(String summary) {
        put(SUMMARY, summary);
    }

    public double getTotalPrice() {
        return getDouble(TOTAL_PRICE);
    }

    public void setTotalPrice(double totalPrice) {
        put(TOTAL_PRICE, totalPrice);
    }

    public String getStatus() {
        return getString(STATUS);
    }

    public void setStatus(String status) {
        put(STATUS, status);
    }
}
