package com.antoshkaplus.recursivelists;

import android.content.Context;
import android.support.v4.app.ActivityCompat;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Anton.Logunov on 4/18/2015.
 */
public class CredentialFactory {

    public static GoogleAccountCredential create(Context context, String accountName) {

        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(context,
                "server:client_id:582892993246-g35aia2vqj3dl9umucp57utfvmvt57u3.apps.googleusercontent.com");
        credential.setSelectedAccountName(accountName);

        return credential;
    }


}
