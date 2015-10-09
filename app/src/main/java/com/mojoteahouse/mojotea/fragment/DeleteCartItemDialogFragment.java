package com.mojoteahouse.mojotea.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mojoteahouse.mojotea.R;

public class DeleteCartItemDialogFragment extends DialogFragment {

    private DeleteCartItemListener deleteCartItemListener;

    public static DeleteCartItemDialogFragment newInstance() {
        DeleteCartItemDialogFragment fragment = new DeleteCartItemDialogFragment();
        fragment.setRetainInstance(true);
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
        builder.setTitle(R.string.delete_item_dialog_title)
                .setMessage(R.string.delete_cart_item_dialog_message)
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
