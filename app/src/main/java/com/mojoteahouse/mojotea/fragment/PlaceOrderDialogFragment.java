package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mojoteahouse.mojotea.R;

public class PlaceOrderDialogFragment extends DialogFragment {

    private PlaceOrderListener placeOrderListener;

    public static PlaceOrderDialogFragment newInstance() {
        PlaceOrderDialogFragment fragment = new PlaceOrderDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public PlaceOrderDialogFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            placeOrderListener = (PlaceOrderListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement "
                    + PlaceOrderListener.class.getSimpleName());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.place_order_dialog_title)
                .setMessage(R.string.place_order_dialog_message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        placeOrderListener.onPlaceOrderConfirmed();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    public interface PlaceOrderListener {

        void onPlaceOrderConfirmed();
    }
}
