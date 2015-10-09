package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.activity.EditCartItemActivity;
import com.mojoteahouse.mojotea.adapter.CartSummaryItemAdapter;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class CartSummaryFragment extends Fragment implements CartSummaryItemAdapter.SummaryItemClickListener {

    private static final int REQUEST_CODE_EDIT_ITEM = 1;

    private CartSummaryItemAdapter cartSummaryItemAdapter;
    private ActionMode actionMode;

    public static CartSummaryFragment newInstance() {
        CartSummaryFragment fragment = new CartSummaryFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public CartSummaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_summary, container, false);

        ListView listView = (ListView) view.findViewById(R.id.summary_list);
        cartSummaryItemAdapter = new CartSummaryItemAdapter(getActivity(), new ArrayList<OrderItem>(), this);
        listView.setAdapter(cartSummaryItemAdapter);
        listView.setMultiChoiceModeListener(new SummaryActionModeListener());

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDataInBackground();
    }

    @Override
    public void onSummaryItemClicked(OrderItem orderItem) {
        Intent intent = new Intent(getActivity(), EditCartItemActivity.class);
        intent.putExtra(EditCartItemActivity.EXTRA_ORDER_ITEM_ID, orderItem.getOrderItemId());
        intent.putExtra(EditCartItemActivity.EXTRA_QUANTITY, orderItem.getQuantity());
        ArrayList<String> selectedToppings = new ArrayList<>();
        selectedToppings.addAll(orderItem.getSelectedToppingsList());
        intent.putStringArrayListExtra(EditCartItemActivity.EXTRA_SELECTED_TOPPINGS, selectedToppings);
        startActivityForResult(intent, REQUEST_CODE_EDIT_ITEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_ITEM && resultCode == Activity.RESULT_OK) {
            loadDataInBackground();
        }
    }

    private void loadDataInBackground() {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.findInBackground(new FindCallback<OrderItem>() {
            @Override
            public void done(List<OrderItem> orderItems, ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), R.string.get_order_item_error_message, Toast.LENGTH_LONG).show();
                } else {
                    cartSummaryItemAdapter.updateOrderItemList(orderItems);
                }
            }
        });
    }


    private class SummaryActionModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            cartSummaryItemAdapter.setSelectionAtPosition(position, checked);
            mode.setTitle(String.format(getString(R.string.action_mode_title_format), cartSummaryItemAdapter.getSelectedCount()));
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.cart_summary_action_mode_menu, menu);
            actionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    showDeleteConfirmationDialog();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            cartSummaryItemAdapter.clearSelection();
            actionMode = null;
        }

        private void showDeleteConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.delete_item_dialog_title)
                    .setMessage(R.string.delete_selected_item_dialog_message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            cartSummaryItemAdapter.clearSelection();
                            actionMode.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }
}
