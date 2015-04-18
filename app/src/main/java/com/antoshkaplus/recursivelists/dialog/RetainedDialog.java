package com.antoshkaplus.recursivelists.dialog;

import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by Anton.Logunov on 3/23/2015.
 */
public class RetainedDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

}