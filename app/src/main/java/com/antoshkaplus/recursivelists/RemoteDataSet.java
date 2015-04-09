package com.antoshkaplus.recursivelists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.SimpleAdapter;


import com.antoshkaplus.quoter.backend.quoteApi.model.Quote;
import com.antoshkaplus.quoter.backend.quoteApi.QuoteApi;
import com.antoshkaplus.quoter.backend.quoteApi.model.QuoteCollection;
import com.antoshkaplus.quoter.backend.quoteApi.model.QuoteList;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by Anton.Logunov on 4/2/2015.
 */
// when we unable to connect broadcast message about that through the whole application
public class QuoteSet extends Observable {

    private volatile List<Quote> quotes;
    private QuoteApi endpoint;
    private Context context;

    public static final String ERROR_EVENT = "com.antoshkaplus.quoter.QuoteSet.ErrorEvent";


    public QuoteSet(Context context, GoogleAccountCredential credential) {
        this.context = context;
        QuoteApi.Builder builder = new QuoteApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), credential);
        endpoint = builder.build();
    }


    public void addQuote(final Quote quote) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuoteApi.InsertQuote query = endpoint.insertQuote(quote);
                    query.execute();
                    reload();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void addQuoteList(final List<Quote> quotes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuoteList list = new QuoteList();
                    list.setQuoteList(quotes);
                    endpoint.insertQuoteList(list).execute();
                    reload();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void updateQuote(final Quote quote) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuoteApi.UpdateQuote query = endpoint.updateQuote(quote);
                    query.execute();
                    reload();
                    setChanged();
                    notifyObservers();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void removeQuote(final Quote quote) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    endpoint.removeQuote(quote.getId()).execute();
                    reload();
                    setChanged();
                    notifyObservers();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void removeAllQuotes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    endpoint.removeAllQuotes().execute();
                    reload();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void reload() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuoteApi.ListQuote query = endpoint.listQuote();
                    quotes = query.execute().getItems();
                    if (quotes == null) quotes = new ArrayList<Quote>();
                    for (Quote q : quotes) {
                        if (q.getAuthor() == null) {
                            q.setAuthor("");
                        }
                        if (q.getMessage() == null) {
                            q.setMessage("");
                        }
                    }
                    setChanged();
                    notifyObservers();
                } catch (IOException ex) {
                    onError();
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void onError() {
        Intent intent = new Intent(ERROR_EVENT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}