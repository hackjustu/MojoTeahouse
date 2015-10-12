package com.mojoteahouse.mojotea.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.mojoteahouse.mojotea.R;

public class PlacingOrderDialogFragment extends DialogFragment {

    public static PlacingOrderDialogFragment newInstance() {
        PlacingOrderDialogFragment fragment = new PlacingOrderDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public PlacingOrderDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_dialog_placing_order)
                .setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
