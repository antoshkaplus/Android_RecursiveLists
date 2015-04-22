package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.antoshkaplus.recursivelists.backend.userItemsApi.UserItemsApi;
import com.antoshkaplus.recursivelists.backend.userItemsApi.model.UserItems;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.google.android.gms.games.request.Requests;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by antoshkaplus on 4/16/15.
 */
public class SyncTask implements Runnable {

    private UserItemsApi api;
    private ItemRepository repo;
    private Listener listener = new Adapter();


    // repository can be an interface
    SyncTask(ItemRepository repo, UserItemsApi api) {
        this.api = api;
        this.repo = repo;
    }

    @Override
    public void run() {
        listener.onStart();
        boolean success = true;
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
            success = false;
            ex.printStackTrace();
        }
        listener.onFinish(success);
    }

    private UpdateResult update(List<Item> existingItems, List<RemovedItem> existingRemovedItems) throws IOException, SQLException {
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
        UUID clientRootId = repo.getRootId();
        String clientRootIdString = clientRootId.toString();
        if (userItems.getItems() != null) {
            for (com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item i : userItems.getItems()) {
                // believe that client items are most RECENT
                if (!existingIds.contains(UUID.fromString(i.getId()))) {
                    if (i.getParentId().equals(userItems.getRootId())) {
                        i.setParentId(clientRootIdString);
                    }
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
        userItems.setRootId(clientRootIdString);
        try {
            api.updateUserItems(userItems).execute();
        } catch (InvalidParameterException ex) {
            return result;
        }

        result.success = true;
        return result;
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public class UpdateResult {
        public boolean success;
        public List<Item> newItems;
        public List<RemovedItem> newRemovedItems;
    }

    interface Listener {
        void onStart();
        void onFinish(boolean success);
    }

    private class Adapter implements Listener {
        @Override
        public void onStart() { }
        @Override
        public void onFinish(boolean success) { }
    }

}
