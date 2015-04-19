package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import com.antoshkaplus.recursivelists.R;

/**
 * Created by Anton.Logunov on 3/23/2015.
 */
public class OkDialog extends RetainedDialog {
    private static final String TAG = "OkDialog";

    private CharSequence title;
    private CharSequence message;

    private static final int TOP_INPUT_PADDING_DP = 15;

    private OkDialogListener listener = new OkDialogListener() {
        @Override
        public void onDialogOk() {}
    };

    public static OkDialog newInstance(CharSequence title, CharSequence message) {
        OkDialog dialog = new OkDialog();
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
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogOk();
                        dismiss();
                    }
                }))
                .create();
        return dialog;
    }

    public void setYesDialogListener(OkDialogListener listener) {
        this.listener = listener;
    }

    public interface OkDialogListener {
        void onDialogOk();
    }
}