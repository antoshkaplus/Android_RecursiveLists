package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.antoshkaplus.recursivelists.backend.userItemsApi.UserItemsApi;
import com.antoshkaplus.recursivelists.backend.userItemsApi.model.UserItems;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.google.android.gms.games.request.Requests;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by antoshkaplus on 4/16/15.
 */
public class SyncTask implements Runnable {

    UserItemsApi api;
    ItemRepository repo;
    // needed to use LocalBroadcastManager
    Context ctx;


    // repository can be an interface
    SyncTask(Context ctx, ItemRepository repo, UserItemsApi api) {
        this.api = api;
        this.repo = repo;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        try {
            List<Item> items = repo.getAllItems();
            List<RemovedItem> removedItems = repo.getAllRemovedItems();
            UpdateResult result;
            while (true) {
                // throws exceptions about endpoint
                result = update(items, removedItems);
                if (result.success) break;
            }
            repo.addItemList(result.newItems);
            repo.addRemovedItemList(result.newRemovedItems);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private UpdateResult update(List<Item> existingItems, List<RemovedItem> existingRemovedItems) throws IOException {
        UpdateResult result = new UpdateResult();
        result.success = false;

        List<Item> items = new ArrayList<>(existingItems);
        List<RemovedItem> removedItems = new ArrayList<>(existingRemovedItems);
        List<Item> newItems = result.newItems = new ArrayList<>();
        List<RemovedItem> newRemovedItems = result.newRemovedItems = new ArrayList<>();

        HashSet<UUID> existingIds = new HashSet<>();
        for (Item i : items) {
            existingIds.add(i.id);
        }

        UserItems userItems = api.getUserItems().execute();
        if (userItems.getItems() != null) {
            for (com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item i : userItems.getItems()) {
                // believe that client items most RECENT
                if (!existingIds.contains(UUID.fromString(i.getId()))) {
                    Item clientItem = Utils.toClientItem(i);
                    items.add(clientItem);
                    newItems.add(clientItem);
                    // new item that's removed
                    if (i.getDeletionDate() != null) {
                        RemovedItem removedItem = new RemovedItem(clientItem, new Date(i.getDeletionDate().getValue()));
                        removedItems.add(removedItem);
                        newRemovedItems.add(removedItem);
                    }
                }
            }
        }

        userItems.setItems(Utils.toBackendItems(items, removedItems));

        try {
            api.updateUserItems(userItems).execute();
        } catch (InvalidParameterException ex) {
            return result;
        }

        result.success = true;
        return result;
    }

    public class UpdateResult {
        public boolean success;
        public List<Item> newItems;
        public List<RemovedItem> newRemovedItems;
    };

}
