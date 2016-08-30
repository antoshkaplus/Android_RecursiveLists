package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.antoshkaplus.fly.dialog.RetainedDialog;
import com.antoshkaplus.recursivelists.R;

/**
 * Created by antoshkaplus on 2/25/15.
 */
public class EditStringDialog extends RetainedDialog {


    private static final String TAG = "EditStringDialog";

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_HINT = "arg_hint";
    public static final String ARG_TEXT = "arg_text";

    private CharSequence title;
    private CharSequence text;
    private CharSequence hint;
    private EditText input;

    private EditStringDialogListener listener = new DefaultListener();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        title = args.getString(ARG_TITLE, "");
        text = args.getString(ARG_TEXT, "");
        hint = args.getString(ARG_HINT, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_string_gialog, null);
        input = (EditText)view.findViewById(R.id.input);
        input.setHint(hint);
        input.setText(text);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        listener.onEditStringDialogSuccess(input.getText());
                        dismiss();
                        return true;
                    }
                    if (i == KeyEvent.KEYCODE_BACK) {
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
                .setView(view)
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
