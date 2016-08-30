package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import com.antoshkaplus.fly.dialog.RetainedDialog;


/**
 * Created by antoshkaplus on 3/3/15.
 */
public class ConfirmDialog extends RetainedDialog {
    private static final String TAG = "ConfirmDialog";

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_MESSAGE = "arg_message";

    private CharSequence title;
    private CharSequence message;

    private ConfirmDialogListener listener = new DefaultListener();


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        title = args.getString(ARG_TITLE, "");
        message = args.getString(ARG_MESSAGE, "");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = (new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onClickYes();
                        dismiss();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onClickCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setConfirmDialogListener(ConfirmDialogListener listener) {
        this.listener = listener;
    }

    public interface ConfirmDialogListener {
        void onClickCancel();
        void onClickYes();
    }

    // using empty implementations
    public static class DefaultListener implements ConfirmDialogListener {
        @Override
        public void onClickCancel() {}
        @Override
        public void onClickYes() {}
    }

}
