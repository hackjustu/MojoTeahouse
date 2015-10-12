package com.mojoteahouse.mojotea.data;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

@ParseClassName("Order")
public class Order extends ParseObject {

    public static final String ANONYMOUS_USER_ID = "anonymousUserId";
    private static final String ORDER_TIME = "orderTime";
    private static final String DELIVER_BY = "deliverBy";
    private static final String COMPLETE_ORDER_LIST = "completeOrderList";
    private static final String TOTAL_QUANTITY = "totalQuantity";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String STATUS = "status";

    public static ParseQuery<Order> getQuery() {
        return ParseQuery.getQuery(Order.class);
    }

    public Order() {

    }

    public String getAnonymousUserId() {
        return getString(ANONYMOUS_USER_ID);
    }

    public void setAnonymousUserId(String anonymousUserId) {
        put(ANONYMOUS_USER_ID, anonymousUserId);
    }

    public String getOrderTime() {
        return getString(ORDER_TIME);
    }

    public void setOrderTime(String orderTime) {
        put(ORDER_TIME, orderTime);
    }

    public String getDeliverBy() {
        return getString(DELIVER_BY);
    }

    public void setDeliverBy(String deliverBy) {
        put(DELIVER_BY, deliverBy);
    }

    public List<String> getCompleteOrderList() {
        return getList(COMPLETE_ORDER_LIST);
    }

    public void setCompleteOrderList(List<String> completeOrderList) {
        put(COMPLETE_ORDER_LIST, completeOrderList);
    }

    public int getTotalQuantity() {
        return getInt(TOTAL_QUANTITY);
    }

    public void setTotalQuantity(int totalQuantity) {
        put(TOTAL_QUANTITY, totalQuantity);
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
}
