package com.mojoteahouse.mojotea.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.adapter.OrderHistoryItemAdapter;
import com.mojoteahouse.mojotea.data.Order;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private TextView noOrderText;
    private OrderHistoryItemAdapter adapter;
    private List<Order> localOrderList;

    public static OrderHistoryFragment newInstance() {
        OrderHistoryFragment fragment = new OrderHistoryFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public OrderHistoryFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.order_history_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new OrderHistoryItemAdapter(getActivity(), new ArrayList<Order>());
        recyclerView.setAdapter(adapter);
        noOrderText = (TextView) view.findViewById(R.id.no_order_text);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOrdersInBackground();
    }

    private void loadOrdersInBackground() {
        ParseQuery<Order> localOrderQuery = Order.getQuery();
        localOrderQuery.fromLocalDatastore();
        localOrderQuery.findInBackground(new FindCallback<Order>() {
            @Override
            public void done(List<Order> orderList, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), R.string.get_order_history_error_message, Toast.LENGTH_LONG).show();
                    recyclerView.setVisibility(View.GONE);
                    noOrderText.setVisibility(View.VISIBLE);
                } else if (orderList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noOrderText.setVisibility(View.VISIBLE);
                } else {
                    noOrderText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    localOrderList = orderList;
                    adapter.updateOrderList(orderList);
                }
            }
        });
    }
}
