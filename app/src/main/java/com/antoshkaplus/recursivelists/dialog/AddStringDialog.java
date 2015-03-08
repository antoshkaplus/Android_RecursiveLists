package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.antoshkaplus.recursivelists.BuildConfig;
import com.antoshkaplus.recursivelists.R;
import com.antoshkaplus.recursivelists.Utils;

/**
 * Created by antoshkaplus on 10/30/14.
 */
public class AddStringDialog extends DialogFragment {

    private static final String TAG = "AddStringDialog";

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_HINT = "arg_hint";

    private CharSequence title;
    private CharSequence hint;
    private EditText input;

    private AddStringDialogListener listener = new DefaultListener();

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
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_add_string_gialog, null);
        input = (EditText)view.findViewById(R.id.input);
        input.setHint(hint);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (BuildConfig.DEBUG) Log.d(TAG, " " + keyEvent.getAction());
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "pressed Add button");
                        listener.onAddStringDialogSuccess(input.getText());
                        dismiss();
                        return true;
                    }
                    if (i == KeyEvent.KEYCODE_BACK) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "pressed Back button");
                        listener.onAddStringDialogCancel();
                        dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        input.setSingleLine();
        input.setImeActionLabel("Add", 0);
        input.requestFocus();
        Dialog dialog = (new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onAddStringDialogSuccess(input.getText());
                        dismiss();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onAddStringDialogCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setAddStringDialogListener(AddStringDialogListener listener) {
        this.listener = listener;
    }

    private class DefaultListener implements AddStringDialogListener {
        @Override
        public void onAddStringDialogSuccess(CharSequence string) {}
        @Override
        public void onAddStringDialogCancel() {}
    }

    public interface AddStringDialogListener {
        void onAddStringDialogSuccess(CharSequence string);
        void onAddStringDialogCancel();
    }
}
