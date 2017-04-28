package com.antoshkaplus.recursivelists.data;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.antoshkaplus.fly.Util;
import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItem;
import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItemList;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.Task;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.ItemState;

import java.io.IOError;
import java.io.IOException;
import java.sql.Date;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.antoshkaplus.recursivelists.data.Util.*;

/**
 * Created by antoshkaplus on 4/22/17.
 */

// before class can be used should get rootId from the internet
public class ItemRepository {

    ItemDbRepository dbRepo;
    ItemsApiFactory itemsApiFactory;
    Context context;
	
	// handles everything
	HandlerThread worker;
	Handler taskHandler;
	
    public ItemRepository(Context ctx, String account, ItemsApiFactory itemsApiFactory) {
        dbRepo = new ItemDbRepository(ctx, account);
        this.itemsApiFactory = itemsApiFactory;
    }
	
	// at some point we would need to quit worker thread
	
    public void addNewItem(Item item, Handler handler) {

        if (Util.isInternetAvailable()) {
			
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    boolean success = true;
                    try {
                        addNewItemOnline(item);
                        sync();
                    } catch (Exception ex) {
                        success = false;
                    }
                    runHandler(handler, success);
                }
			};
            taskHandler.post(r);
			
        } else {

            item.state = ItemState.Local;
            dbRepo.addItem(item);

			runHandler(handler, true);
        }

    }

    private void updateLocal() throws Exception {
        ItemsApi api = itemsApiFactory.create();

        int localVersion = dbRepo.getLastSyncVersion();

        // need to get last sync version
        int remoteVersion = api.getDbVersion().execute().getValue();
        VariantItemList itemList = api.getItemListGVersion(localVersion).execute();

        List<Item> items = itemList.getVariantItems().stream().map(s-> toLocalItem(s)).collect(Collectors.toList());

        dbRepo.updateAllItemsOffline(items);
        dbRepo.updateLastSyncVersion(remoteVersion);
    }

    private void updateRemote() throws IOException {
        ItemsApi api = itemsApiFactory.create();

        dbRepo.markInProgress();
        List<Item> items = dbRepo.getItemListByState(ItemState.InProgress);
        List<VariantItem> variantItems = items.stream().map(s -> toRemoteItem(s, null)).collect(Collectors.toList());
        VariantItemList list = new VariantItemList();
        list.setVariantItems(variantItems);
        api.addItemList(list).execute();

        dbRepo.markRemote(items);
    }


    // this is method is purely internet
    public void sync(Handler handler) {
        boolean success = true;
        try {
            sync();
        } catch (Exception ex) {
            success = false;
        }
        runHandler(handler, success);
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
        return dbRepo.getRootId();
    }

    public boolean hasRemovedItems() {
		return dbRepo.hasRemovedItems();
	}
	
	// user has to handle
    public void addItemList(List<Item> result, Handler handler) {
		// try online.
	}

	public void addItemListOffline(List<Item> result) {
        dbRepo.addItemList(result);
    }

    public Item getItem(UUID parentId) {
        return dbRepo.getItem(parentId);
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
		return dbRepo.getChildrenCount(parentId);
    }

    public Collection<? extends Item> getChildren(UUID parentId) {
		return dbRepo.getChildren(parentId);
    }

	// check handler for null, add some description for user to post
	// make Handler an optional type
    void runHandler(Handler handler, boolean outcome) {
        taskHandler.post(new Runnable() {
			public void run() {
                Message msg = handler.obtainMessage();
                Bundle b = msg.getData();
                b.putBoolean("success", outcome);
                handler.sendMessage(msg);
			}
		});
    }

    public void Start() {
		worker = new HandlerThread("ItemRepositoryHandlerThread");
		worker.start();
		taskHandler = new Handler(worker.getLooper());
	}

    public void Destroy() {
		worker.quit();
	}


    private Item toLocalItem(VariantItem item) {
        if (item.getItem() != null) {
            Item updateItem = new Item();
            updateItem.id = UUID.fromString(item.getItem().getUuid());
            updateItem.parentId = UUID.fromString(item.getItem().getParentUuid());
            updateItem.title = item.getItem().getTitle();
            return updateItem;
        } else if (item.getTask() != null) {
            Task updateTask = new Task();
            updateTask.id = UUID.fromString(item.getTask().getUuid());
            updateTask.parentId = UUID.fromString(item.getTask().getParentUuid());
            updateTask.title = item.getTask().getTitle();
            if (item.getTask().getCompleteDate() != null) {
                Date d = new Date(item.getTask().getCompleteDate().getValue());
                updateTask.setCompleteDate(d);
            }
            return updateTask;
        } else {
            return null;
        }
    }

    private VariantItem toRemoteItem(Item item, RemovedItem removedItem) {
        VariantItem vItem = new VariantItem();
        switch (item.getItemKind()) {
            case Item:
                com.antoshkaplus.recursivelists.backend.itemsApi.model.Item rItem = new com.antoshkaplus.recursivelists.backend.itemsApi.model.Item();
                rItem.setTitle(item.title);
                rItem.setUuid(item.id.toString());
                rItem.setParentUuid(item.parentId.toString());
                vItem.setItem(rItem);
            case Task:
                Task task = (Task)item;
                com.antoshkaplus.recursivelists.backend.itemsApi.model.Task rTask = new com.antoshkaplus.recursivelists.backend.itemsApi.model.Task();
                rTask.setTitle(item.title);
                rTask.setUuid(item.id.toString());
                rTask.setParentUuid(item.parentId.toString());
                vItem.setTask(rTask);
            default:
                throw new RuntimeException("Unknown ItemKind");
        }
    }



}
