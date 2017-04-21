package com.antoshkaplus.recursivelists.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.antoshkaplus.recursivelists.CredentialFactory;
import com.antoshkaplus.recursivelists.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


public class SettingsActivity extends Activity {

    static final int REQUEST_ACCOUNT_PICKER = 2;

    SharedPreferences settings = null;

    SettingsFragment settingsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment = new SettingsFragment())
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            Preference pref = getPreferenceManager().findPreference(getString(R.string.pref__account__key));
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            pref.setSummary(sharedPreferences.getString(getActivity().getString(R.string.pref__account__key), ""));
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // maybe current account will be selected if i mention it
                    GoogleAccountCredential credential = CredentialFactory.create(getActivity(), null);
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName =
                                data.getExtras().getString(
                                        AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            Preference pref = getPreferenceManager().findPreference(getString(R.string.pref__account__key));
                            pref.setSummary(accountName);
                            SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getActivity());
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(getString(R.string.pref__account__key), accountName);
                            editor.apply();
                        }
                    }
                    break;
            }
        }
    }
}