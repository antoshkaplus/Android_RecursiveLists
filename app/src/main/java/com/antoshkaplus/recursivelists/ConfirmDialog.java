package com.antoshkaplus.recursivelists;

/**
 * Created by antoshkaplus on 3/3/15.
 */
public class ConfirmDialog {
    private static final String TAG = "NoYesDialog";

    private CharSequence title;
    private CharSequence message;

    private static final int TOP_INPUT_PADDING_DP = 15;

    private YesDialogListener listener = new YesDialogAdapter();

    public static YesDialog newInstance(CharSequence title, CharSequence message) {
        YesDialog dialog = new YesDialog();
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
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogYes();
                        dismiss();
                    }
                }))
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogNo();
                        dismiss();
                    }
                })
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public void setYesDialogListener(YesDialogListener listener) {
        this.listener = listener;
    }

    public interface YesDialogListener {
        void onDialogNo();
        void onDialogYes();
    }

    // using empty implementations
    public static class YesDialogAdapter implements YesDialogListener {
        @Override
        public void onDialogNo() {}
        @Override
        public void onDialogYes() {}
    }

}
