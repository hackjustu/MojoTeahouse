package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.Order;

import java.util.List;

public class OrderHistoryItemAdapter extends RecyclerView.Adapter<OrderHistoryItemAdapter.OrderViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Order> orderList;

    public OrderHistoryItemAdapter(Context context, List<Order> orderList) {
        layoutInflater = LayoutInflater.from(context);
        this.orderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.order_history_list_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
    }

    @Override
    public int getItemCount() {
        return orderList == null
                ? 0
                : orderList.size();
    }

    protected class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView summaryText;
        private TextView priceText;

        public OrderViewHolder(View itemView) {
            super(itemView);

            summaryText = (TextView) itemView.findViewById(R.id.summary_text);
            priceText = (TextView) itemView.findViewById(R.id.price_text);
        }
    }
}
