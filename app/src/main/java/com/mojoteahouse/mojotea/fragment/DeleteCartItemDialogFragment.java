package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mojoteahouse.mojotea.R;

public class DeleteCartItemDialogFragment extends DialogFragment {

    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    private DeleteCartItemListener deleteCartItemListener;

    public static DeleteCartItemDialogFragment newInstance() {
        DeleteCartItemDialogFragment fragment = new DeleteCartItemDialogFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public static DeleteCartItemDialogFragment newInstance(String message) {
        DeleteCartItemDialogFragment fragment = new DeleteCartItemDialogFragment();
        fragment.setRetainInstance(true);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE, message);
        fragment.setArguments(bundle);
        return fragment;
    }

    public DeleteCartItemDialogFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            deleteCartItemListener = (DeleteCartItemListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement "
                    + DeleteCartItemListener.class.getSimpleName());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        Bundle bundle = getArguments();
        if (bundle != null) {
            builder.setMessage(bundle.getString(EXTRA_MESSAGE));
        } else {
            builder.setMessage(R.string.delete_cart_item_dialog_message);
        }
        builder.setTitle(R.string.delete_item_dialog_title)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCartItemListener.onDeleteConfirmed();
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


    public interface DeleteCartItemListener {

        void onDeleteConfirmed();
    }
}
