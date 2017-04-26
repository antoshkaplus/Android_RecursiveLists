package com.antoshkaplus.recursivelists.data;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.antoshkaplus.fly.Util;
import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItemList;
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
        // try to push outside first
        // if was able to push outside
        // have to make sure that I get it back

        // after each write we should call sync procedure
        if (Util.isInternetAvailable()) {
			
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (addNewItemOnline(item, handler)) {
                        // it's bad that user gets feedback eventhough sync is not run
                        sync(handler);
                    }
                }
			};
            taskHandler.post(r);
			
        } else {

            // other wise we are fine doing it in sync
            item.state = ItemState.Local;
            dbRepo.addItem(item);

            // notifications should come from the repo
            // about data change

			runHandler(handler, false);
            // catch exception let user know if was able to save
        }

    }

    private void updateLocal() {
        ItemsApi api = itemsApiFactory.create();

        int localVersion = dbRepo.getLastSyncVersion();

        // need to get last sync version
        api.get
		VariantItemList itemList = api.getItemListGVersion(localVersion);
		List<com.antoshkaplus.recursivelists.backend.itemsApi.model.Item> items = itemList.();

        dbRepo.updateAllItemsOffline(items);

        dbRepo.updateLastSyncVersion()
    }

    private void updateRemote() {


		// extract from repository everything that is IN-PROGRESS or LOCAL
		// and in the same transaction mark it IN-PROGRESS
		
		// send it to server
		
		// for each we do add new. if new fails, item is already there and it's good
		
		// mark all items remote
    }


    // this is method is purely internet
    public void sync(Handler handler) {
        updateLocal();
        updateRemote();

        runHandler(handler, false);
    }


    private boolean addNewItemOnline(Item item, Handler handler) {
        ItemsApi api = itemsApiFactory.create();
        // so we have to use fucking if statement and it will happen many many times.
        if (item.getItemKind() == ItemKind.Item) {
            api.addItemOnline(getApiItem(item, Optional.empty())).execute();
        } else {
            api.addTaskOnline(item).execute();
        }
		runHandler(handler, false);
        // in case of exception
        // can actually sort out exceptions to figure out if can do it offline

		return false;

        return true;
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

    // conversion methods
}
