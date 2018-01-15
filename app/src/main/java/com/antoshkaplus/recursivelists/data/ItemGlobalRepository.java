package com.antoshkaplus.recursivelists.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItem;
import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItemList;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemState;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.antoshkaplus.recursivelists.data.Util.*;

/**
 * Created by antoshkaplus on 4/22/17.
 */

// before class can be used should get rootId from the internet
public class ItemGlobalRepository {

    ItemLocalDbRepository localDbRepo;
    ItemsApiFactory itemsApiFactory;
	
    public ItemGlobalRepository(Context ctx, String account, ItemsApiFactory itemsApiFactory, Looper looper) {
        localDbRepo = new ItemLocalDbRepository(ctx, account);
        this.itemsApiFactory = itemsApiFactory;
    }

    public void addNewItem(Item item, Handler handler) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                try {
                    addNewItemOnline(item);
                    sync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    success = false;
                }
                if (!success) {
                    try {
                        localDbRepo.addItem(item);
                        success = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        success = false;
                    }
                }
                runHandler(handler, success);
            }
        });
    }

    private void updateLocal() throws Exception {
        ItemsApi api = itemsApiFactory.create();

        int localVersion = localDbRepo.getLastSyncVersion();

        // need to get last sync version
        int remoteVersion = api.getDbVersion().execute().getValue();
        VariantItemList itemList = api.getItemListGVersion(localVersion).execute();
        if (itemList.getVariantItems() != null) {
            List<Item> items = itemList.getVariantItems().stream().map(s -> Util.toLocalItem(s)).collect(Collectors.toList());
            localDbRepo.updateAllItemsOffline(items);
        }
        localDbRepo.updateLastSyncVersion(remoteVersion);
    }

    private void updateRemote() throws IOException {
        ItemsApi api = itemsApiFactory.create();

        localDbRepo.markInProgress();
        List<Item> items = localDbRepo.getItemListByState(ItemState.InProgress);
        List<VariantItem> variantItems = items.stream().map(s -> toRemoteItem(s, null)).collect(Collectors.toList());
        VariantItemList list = new VariantItemList();
        list.setVariantItems(variantItems);
        api.addItemList(list).execute();

        localDbRepo.markRemote(items);
    }


    // this is method is purely internet
    public void sync(Handler handler) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                try {
                    sync();
                } catch (Exception ex) {
                    // can get more info from exception
                    ex.printStackTrace();
                    success = false;
                }
                runHandler(handler, success);
            }
        });
    }

    private void sync() throws Exception {
        updateLocal();
        updateRemote();
    }


    private void addNewItemOnline(Item item) throws Exception {
        ItemsApi api = itemsApiFactory.create();
        // so we have to use fucking if statement and it will happen many many times.
        api.addItem(toRemoteItem(item, null)).execute();
    }

    public UUID getRootId() {
        return localDbRepo.getRootId();
    }

    public boolean hasRemovedItems() {
		return localDbRepo.hasRemovedItems();
	}
	
	// user has to handle
    public void addItemList(List<Item> result, Handler handler) {
		// try online.
	}

	public void addItemListOffline(List<Item> result) {
        localDbRepo.addItemList(result);
    }

    public Item getItem(UUID parentId) {
        return localDbRepo.getItem(parentId);
	}

    public void updateItem(Item moveItem) {    
		// online first
		// then try offline if local one
	}

	// items has to be onces from the server
	// can we force server involvement???
    public void updateAllItems(List<Item> items) {
		// update is based on where you are.
		//
        throw new RuntimeException("not supported");
	}

    public void deleteItem(Item item) {
		// go online
		// offline only if newly created
	}

    public void deleteChildren(Item i) {
		// go online only
		// can accomplish by deleteItem one by one
	}

    public void undoLastRemoval() {
		// retrieve last successful removal and try to undo online
		// else we unable to do anything about it.
	}

    public int getChildrenCount(UUID parentId) {
		return localDbRepo.getChildrenCount(parentId);
    }

    public Collection<? extends Item> getChildren(UUID parentId) {
		return localDbRepo.getChildren(parentId);
    }

	// check handler for null, add some description for user to post
	// make Handler an optional type
    void runHandler(Handler handler, boolean outcome) {
        Message msg = handler.obtainMessage();
        Bundle b = msg.getData();
        b.putBoolean("success", outcome);
        handler.sendMessage(msg);
    }



}
