package com.mojoteahouse.mojotea.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.mojoteahouse.mojotea.R;

public class ClosedNowDialogFragment extends DialogFragment {

    public static ClosedNowDialogFragment newInstance() {
        ClosedNowDialogFragment fragment = new ClosedNowDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public ClosedNowDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_dialog_closed_now)
                .setCancelable(false);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
