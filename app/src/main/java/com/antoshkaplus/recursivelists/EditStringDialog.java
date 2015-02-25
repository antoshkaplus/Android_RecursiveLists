package com.antoshkaplus.recursivelists;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by antoshkaplus on 2/25/15.
 */
public class EditStringDialog extends DialogFragment {


    private static final String TAG = "EditStringDialog";

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_HINT = "arg_hint";

    private CharSequence title;
    private CharSequence hint;
    private EditText input;

    private static final int TOP_INPUT_PADDING_DP = 15;

    private EditStringDialogListener listener = new DefaultListener();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        title = args.getString(ARG_TITLE, "");
        hint = args.getString(ARG_HINT, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        input = new EditText(getActivity());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        input.setPadding(
                input.getPaddingLeft(),
                (int)Utils.dpToPx(getResources(), TOP_INPUT_PADDING_DP),
                input.getPaddingRight(),
                input.getPaddingBottom());
        input.setLayoutParams(params);
        input.setHint(hint);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d(TAG, " " + keyEvent.getAction());
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        Log.d(TAG, "pressed Edit button");
                        listener.onEditStringDialogSuccess(input.getText());
                        dismiss();
                        return true;
                    }
                    if (i == KeyEvent.KEYCODE_BACK) {
                        Log.d(TAG, "pressed Back button");
                        listener.onEditStringDialogCancel();
                        dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        input.setSingleLine();
        input.setImeActionLabel("Edit", 0);
        input.requestFocus();
        Dialog dialog = (new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onEditStringDialogSuccess(input.getText());
                        dismiss();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onEditStringDialogCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setEditStringDialogListener(EditStringDialogListener listener) {
        this.listener = listener;
    }

    private class DefaultListener implements EditStringDialogListener {
        @Override
        public void onEditStringDialogSuccess(CharSequence string) {}
        @Override
        public void onEditStringDialogCancel() {}
    }

    public interface EditStringDialogListener {
        void onEditStringDialogSuccess(CharSequence string);
        void onEditStringDialogCancel();
    }

}
