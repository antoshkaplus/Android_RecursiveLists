package com.antoshkaplus.recursivelists.data;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.antoshkaplus.fly.Util;
import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.ItemState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.antoshkaplus.recursivelists.data.Util.*;

/**
 * Created by antoshkaplus on 4/22/17.
 */

public class ItemRepository {

    ItemDbRepository dbRepo;
    ItemsApiFactory itemsApiFactory;
    Context context;

    public ItemRepository(Context ctx, String account, ItemsApiFactory itemsApiFactory) {
        dbRepo = new ItemDbRepository(ctx, account);
        this.itemsApiFactory = itemsApiFactory;
    }

    public void addNewItem(Item item, Handler handler) {
        // try to push outside first
        // if was able to push outside
        // have to make sure that I get it back

        // after each write we should call sync procedure
        if (Util.isInternetAvailable()) {

            Runnable r = new Runnable() {


                // do something with the handler on top
            if (!addNewItemOnline(item, handler)) {
                // send user that was unable to add item for some reason
            } else {
                sync();

            }
            }


            HandlerThread t;
            t.run();

            Handler hh = new Handler(t.getLooper());
            hh.post(r);

        } else {

            // other wise we are fine doing it in sync
            item.state = ItemState.Local;
            dbRepo.addItem(item);

            // notifications should come from the repo
            // about data change

            // plus maybe some handler about success of the operation made

            // catch exception let user know if was able to save
        }

    }

    private void updateLocal() {
        ItemsApi api = itemsApiFactory.create();
        api.getGtaskLastUpdate()
    }

    private void updateRemote() {

    }


    // this is method is purely internet
    public void sync(OutcomeHandler handler) {
        updateLocal();
        updateRemote();

        handler.handle(false);
    }


    private boolean addNewItemOnline(Item item, OutcomeHandler handler) {
        ItemsApi api = itemsApiFactory.create();
        // so we have to use fucking if statement and it will happen many many times.
        if (item.getItemKind() == ItemKind.Item) {
            api.addItemOnline(getApiItem(item, Optional.empty())).execute();
        } else {
            api.addTaskOnline(item).execute();
        }

        // in case of exception
        // can actually sort out exceptions to figure out if can do it offline
        return false;

        return true;
    }

    public UUID getRootId() {
        return null;
    }

    public boolean hasRemovedItems() {
    }

    public void addItemList(List<Item> result) {
    }

    public Item getItem(UUID parentId) {
    }

    public void updateItem(Item moveItem) {
    }

    public void updateAllItems(List<Item> items) {
    }

    public void deleteItem(Item item) {
    }

    public void deleteChildren(Item i) {
    }

    public void undoLastRemoval() {
    }

    public int getChildrenCount(UUID parentId) {
    }

    public Collection<? extends Item> getChildren(UUID parentId) {
        return null;
    }


    void runOnUiThread(OutcomeHandler handler, boolean outcome) {

    }


    public interface OutcomeHandler {
        void handle(boolean success);
    }

    // conversion methods
}
