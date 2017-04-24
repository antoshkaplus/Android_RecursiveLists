package com.antoshkaplus.recursivelists.data;

import com.antoshkaplus.recursivelists.BuildConfig;
import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

import java.io.IOError;
import java.io.IOException;

/**
 * Created by antoshkaplus on 4/22/17.
 */

public class ItemsApiFactory {

    private HttpRequestInitializer credential;



    public ItemsApiFactory(HttpRequestInitializer credential) {
        this.credential = credential;
    }

    public ItemsApi create() {

        ItemsApi.Builder builder = new ItemsApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), credential);
        builder.setRootUrl(BuildConfig.HOST);
        builder.setHttpRequestInitializer(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                credential.initialize(httpRequest);
                httpRequest.setConnectTimeout(20 * 1000);  // 3 seconds connect timeout
                httpRequest.setReadTimeout(20 * 1000);  // 3 seconds read timeout
            }
        });
        // TODO may need this line
        //builder.setApplicationName("antoshkaplus-recursivelists");
        return builder.build();
    }

    public void setCredential(HttpRequestInitializer credential) {
        this.credential = credential;
    }
}
