package com.antoshkaplus.recursivelists.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.antoshkaplus.fly.dialog.RetainedDialog;
import com.antoshkaplus.recursivelists.R;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.Task;

/**
 * Created by antoshkaplus on 10/30/14.
 */
public class AddItemDialog extends RetainedDialog {

    private static final String TAG = "AddStringDialog";

    public static final String ARG_TITLE = "arg_title";
    public static final String ARG_HINT = "arg_hint";
    public static final String ARG_PARENT_KIND = "arg_parent_kind";

    private CharSequence title;
    private CharSequence hint;
    private EditText input;
    private Spinner kindView;
    private ItemKind parentKind;

    private AddItemDialogListener listener = new DefaultListener();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        title = args.getString(ARG_TITLE, "");
        hint = args.getString(ARG_HINT, "");
        parentKind = (ItemKind) args.getSerializable(ARG_PARENT_KIND);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.view_add_item, null);

        kindView = (Spinner)view.findViewById(R.id.kind);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.item_kind_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kindView.setAdapter(adapter);

        if (parentKind == ItemKind.Task) {
            // TODO should be removing by index bacause of multilanguage support
            adapter.remove("Item");
        }

        kindView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onKindSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        input = (EditText)view.findViewById(R.id.title);
        input.setHint(hint);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (i == KeyEvent.KEYCODE_ENTER) {
                        onSuccess();
                        return true;
                    }
                    if (i == KeyEvent.KEYCODE_BACK) {
                        listener.onAddItemDialogCancel();
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
                        onSuccess();
                    }
                }))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onAddItemDialogCancel();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setOnShowListener(d -> {
            onKindSelected();
        });
        return dialog;
    }

    public void setAddItemDialogListener(AddItemDialogListener listener) {
        this.listener = listener;
    }

    private void onKindSelected() {
        ItemKind kind = ItemKind.fromResourceIndex((int)kindView.getSelectedItemId());
        int colorId = 0;
        if (kind == ItemKind.Item) {
            colorId = R.color.item;
        }
        if (kind == ItemKind.Task) {
            colorId = R.color.task;
        }
        Drawable color = getContext().getDrawable(colorId);;
        ((AlertDialog)getDialog()).getWindow().setBackgroundDrawable(color);
    }

    private void onSuccess() {
        Item item;
        ItemKind kind = ItemKind.fromResourceIndex((int)kindView.getSelectedItemId());
        if (kind == ItemKind.Item) {
            item = new Item();
        } else {
            item = new Task();
        }
        item.title = input.getText().toString();

        listener.onAddItemDialogSuccess(item);
        dismiss();
    }

    private class DefaultListener implements AddItemDialogListener {
        @Override
        public void onAddItemDialogSuccess(Item result) {}
        @Override
        public void onAddItemDialogCancel() {}
    }

    public interface AddItemDialogListener {
        void onAddItemDialogSuccess(Item item);
        void onAddItemDialogCancel();
    }
}
