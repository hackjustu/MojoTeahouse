package com.mojoteahouse.mojotea.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class CartSummaryItemAdapter extends ArrayAdapter<OrderItem> {

    private LayoutInflater layoutInflater;
    private List<OrderItem> orderItemList;
    private SummaryItemClickListener summaryItemClickListener;
    private String quantityFormat;
    private String priceFormat;
    private SparseBooleanArray selectedItemPositions;

    public CartSummaryItemAdapter(Context context,
                                  List<OrderItem> orderItemList,
                                  SummaryItemClickListener summaryItemClickListener) {
        super(context, R.layout.cart_summary_list_item, orderItemList);
        layoutInflater = LayoutInflater.from(context);
        this.orderItemList = orderItemList;
        this.summaryItemClickListener = summaryItemClickListener;
        quantityFormat = context.getString(R.string.quantity_format);
        priceFormat = context.getString(R.string.price_format);
        selectedItemPositions = new SparseBooleanArray();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final OrderItem orderItem = orderItemList.get(position);
        final CartSummaryViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.cart_summary_list_item, parent, false);
            holder = new CartSummaryViewHolder();
            holder.summaryLayout = (LinearLayout) convertView.findViewById(R.id.summary_layout);
            holder.imageView = (ParseImageView) convertView.findViewById(R.id.image_view);
            holder.nameText = (TextView) convertView.findViewById(R.id.name_text);
            holder.quantityText = (TextView) convertView.findViewById(R.id.quantity_text);
            holder.toppingText = (TextView) convertView.findViewById(R.id.topping_text);
            holder.priceText = (TextView) convertView.findViewById(R.id.price_text);

            convertView.setTag(holder);
        } else {
            holder = (CartSummaryViewHolder) convertView.getTag();
        }

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
        if (selectedItemPositions.get(position)) {
            holder.summaryLayout.setActivated(true);
        } else {
            holder.summaryLayout.setActivated(false);
        }
        holder.summaryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                summaryItemClickListener.onSummaryItemClicked(orderItem);
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return orderItemList == null
                ? 0
                : orderItemList.size();
    }

    public void updateOrderItemList(List<OrderItem> orderItemList) {
        if (orderItemList != null) {
            Collections.sort(orderItemList, new OrderItemComparator());
            clearSelection();
            this.orderItemList.clear();
            this.orderItemList.addAll(orderItemList);
            notifyDataSetChanged();
        }
    }

    public void clearSelection() {
        selectedItemPositions.clear();
        notifyDataSetChanged();
    }

    public void setSelectionAtPosition(int position, boolean value) {
        if (value) {
            selectedItemPositions.put(position, true);
        } else {
            selectedItemPositions.delete(position);
        }

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItemPositions.size();
    }

    public SparseBooleanArray getSelectedItemPositions() {
        return selectedItemPositions;
    }


    public interface SummaryItemClickListener {

        void onSummaryItemClicked(OrderItem orderItem);
    }

    private class CartSummaryViewHolder {

        private LinearLayout summaryLayout;
        private ParseImageView imageView;
        private TextView nameText;
        private TextView quantityText;
        private TextView toppingText;
        private TextView priceText;
    }

    private class OrderItemComparator implements Comparator<OrderItem> {

        @Override
        public int compare(OrderItem first, OrderItem second) {
            return first.getOrderItemId().compareTo(second.getOrderItemId());
        }
    }
}
