package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.MojoImage;
import com.mojoteahouse.mojotea.data.MojoMenu;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.util.DataUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CartSummaryItemAdapter extends RecyclerView.Adapter<CartSummaryItemAdapter.CartSummaryViewHolder> {

    private LayoutInflater layoutInflater;
    private List<OrderItem> orderItemList;
    private CartSummaryItemClickListener itemClickListener;
    private String quantityFormat;
    private String priceFormat;

    public CartSummaryItemAdapter(Context context, List<OrderItem> orderItemList, CartSummaryItemClickListener listener) {
        layoutInflater = LayoutInflater.from(context);
        this.orderItemList = orderItemList;
        itemClickListener = listener;
        quantityFormat = context.getString(R.string.quantity_format);
        priceFormat = context.getString(R.string.price_format);
    }

    @Override
    public CartSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.cart_summary_list_item, parent, false);
        return new CartSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CartSummaryViewHolder holder, int position) {
        OrderItem orderItem = orderItemList.get(position);

        MojoMenu associatedMojoMenu = orderItem.getAssociatedMojoMenu();
        ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
        mojoImageQuery.fromLocalDatastore();
        mojoImageQuery.whereEqualTo(MojoImage.IMAGE_ID, associatedMojoMenu.getImageId());
        mojoImageQuery.getFirstInBackground(new GetCallback<MojoImage>() {
            @Override
            public void done(MojoImage mojoImage, ParseException e) {
                if (e == null) {
                    holder.imageView.setParseFile(mojoImage.getImage());
                    holder.imageView.loadInBackground();
                }
            }
        });

        holder.nameText.setText(orderItem.getName());
        holder.quantityText.setText(String.format(quantityFormat, orderItem.getQuantity()));
        holder.toppingText.setText(DataUtils.getToppingListString(orderItem.getSelectedToppingsList()));
        holder.priceText.setText(String.format(priceFormat, orderItem.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return orderItemList == null
                ? 0
                : orderItemList.size();
    }

    public void updateOrderItemList(List<OrderItem> orderItemList) {
        if (orderItemList != null) {
            Collections.sort(orderItemList, new OrderItemComparator());
            this.orderItemList.clear();
            this.orderItemList.addAll(orderItemList);
            notifyDataSetChanged();
        }
    }


    public interface CartSummaryItemClickListener {

        void onCartSummaryItemClicked(OrderItem orderItem);
    }

    protected class CartSummaryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LinearLayout summaryLayout;
        private ParseImageView imageView;
        private TextView nameText;
        private TextView quantityText;
        private TextView toppingText;
        private TextView priceText;

        public CartSummaryViewHolder(View itemView) {
            super(itemView);

            summaryLayout = (LinearLayout) itemView.findViewById(R.id.summary_layout);
            imageView = (ParseImageView) itemView.findViewById(R.id.image_view);
            nameText = (TextView) itemView.findViewById(R.id.name_text);
            quantityText = (TextView) itemView.findViewById(R.id.quantity_text);
            toppingText = (TextView) itemView.findViewById(R.id.topping_text);
            priceText = (TextView) itemView.findViewById(R.id.price_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OrderItem orderItem = orderItemList.get(getLayoutPosition());
            itemClickListener.onCartSummaryItemClicked(orderItem);
        }
    }

    private class OrderItemComparator implements Comparator<OrderItem> {

        @Override
        public int compare(OrderItem first, OrderItem second) {
            return first.getOrderItemId().compareTo(second.getOrderItemId());
        }
    }

    //    private LayoutInflater layoutInflater;
//    private List<OrderItem> orderItemList;
//    private String quantityFormat;
//    private String priceFormat;
//    private int backgroundNormal;
//    private int backgroundSelected;
//    private SparseBooleanArray selectedItemPositions;
//
//    public CartSummaryItemAdapter(Context context, List<OrderItem> orderItemList) {
//        super(context, R.layout.cart_summary_list_item, orderItemList);
//        layoutInflater = LayoutInflater.from(context);
//        this.orderItemList = orderItemList;
//        quantityFormat = context.getString(R.string.quantity_format);
//        priceFormat = context.getString(R.string.price_format);
//        backgroundNormal = context.getResources().getColor(R.color.white);
//        backgroundSelected = context.getResources().getColor(R.color.light_primary);
//        selectedItemPositions = new SparseBooleanArray();
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        final OrderItem orderItem = orderItemList.get(position);
//        final CartSummaryViewHolder holder;
//        if (convertView == null) {
//            convertView = layoutInflater.inflate(R.layout.cart_summary_list_item, parent, false);
//            holder = new CartSummaryViewHolder();
//            holder.summaryLayout = (LinearLayout) convertView.findViewById(R.id.summary_layout);
//            holder.imageView = (ParseImageView) convertView.findViewById(R.id.image_view);
//            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
//            holder.quantityText = (TextView) convertView.findViewById(R.id.quantity_text);
//            holder.toppingText = (TextView) convertView.findViewById(R.id.topping_text);
//            holder.priceText = (TextView) convertView.findViewById(R.id.price_text);
//
//            convertView.setTag(holder);
//        } else {
//            holder = (CartSummaryViewHolder) convertView.getTag();
//        }
//
//        MojoMenu associatedMojoMenu = orderItem.getAssociatedMojoMenu();
//        ParseQuery<MojoImage> mojoImageQuery = MojoImage.getQuery();
//        mojoImageQuery.fromLocalDatastore();
//        mojoImageQuery.whereEqualTo(MojoImage.IMAGE_ID, associatedMojoMenu.getImageId());
//        mojoImageQuery.getFirstInBackground(new GetCallback<MojoImage>() {
//            @Override
//            public void done(MojoImage mojoImage, ParseException e) {
//                if (e == null) {
//                    holder.imageView.setParseFile(mojoImage.getImage());
//                    holder.imageView.loadInBackground();
//                }
//            }
//        });
//
//        holder.nameText.setText(orderItem.getName());
//        holder.quantityText.setText(String.format(quantityFormat, orderItem.getQuantity()));
//        holder.toppingText.setText(DataUtils.getToppingListString(orderItem.getSelectedToppingsList()));
//        holder.priceText.setText(String.format(priceFormat, orderItem.getTotalPrice()));
//        if (selectedItemPositions.get(position)) {
//            holder.summaryLayout.setBackgroundColor(backgroundSelected);
//        } else {
//            holder.summaryLayout.setBackgroundColor(backgroundNormal);
//        }
//
//        return convertView;
//    }
//
//    @Override
//    public int getCount() {
//        return orderItemList == null
//                ? 0
//                : orderItemList.size();
//    }
//
//    public void updateOrderItemList(List<OrderItem> orderItemList) {
//        if (orderItemList != null) {
//            Collections.sort(orderItemList, new OrderItemComparator());
//            clearSelection();
//            this.orderItemList.clear();
//            this.orderItemList.addAll(orderItemList);
//            notifyDataSetChanged();
//        }
//    }
//
//    public OrderItem getOrderItemAtPosition(int position) {
//        return orderItemList.get(position);
//    }
//
//    public void clearSelection() {
//        selectedItemPositions.clear();
//        notifyDataSetChanged();
//    }
//
//    public void setSelectionAtPosition(int position, boolean selected) {
//        if (selected) {
//            selectedItemPositions.put(position, true);
//        } else {
//            selectedItemPositions.delete(position);
//        }
//
//        notifyDataSetChanged();
//    }
//
//    public boolean isSelectedAtPosition(int position) {
//        return selectedItemPositions.get(position, false);
//    }
//
//    public int getSelectedCount() {
//        return selectedItemPositions.size();
//    }
//
//    public SparseBooleanArray getSelectedItemPositions() {
//        return selectedItemPositions;
//    }
//
//
//    private class CartSummaryViewHolder {
//
//        private LinearLayout summaryLayout;
//        private ParseImageView imageView;
//        private TextView nameText;
//        private TextView quantityText;
//        private TextView toppingText;
//        private TextView priceText;
//    }
}
