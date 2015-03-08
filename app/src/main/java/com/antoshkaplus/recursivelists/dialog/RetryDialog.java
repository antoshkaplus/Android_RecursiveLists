package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * Created by Anton.Logunov on 3/8/2015.
 */
public class RetryDialog extends DialogFragment {
    private static final String TAG = "CancelRetryDialog";

    private CharSequence title;
    private CharSequence message;

    private RetryDialogListener listener = new RetryDialogAdapter();

    public static RetryDialog newInstance(CharSequence title, CharSequence message) {
        RetryDialog dialog = new RetryDialog();
        dialog.title = title;
        dialog.message = message;
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = (new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogRetry();
                        dismiss();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setRetryDialogListener(RetryDialogListener listener) {
        this.listener = listener;
    }

    public interface RetryDialogListener {
        void onDialogCancel();
        void onDialogRetry();
    }

    // using empty implementations
    public class RetryDialogAdapter implements RetryDialogListener {
        @Override
        public void onDialogCancel() {}
        @Override
        public void onDialogRetry() {}
    }

}