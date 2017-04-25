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

// before class can be used should get rootId from the internet
public class ItemRepository {
	
	class VariantItem {
		Item item;
		Task task;
		
		ItemKind kind;
		
		private VariantItem() {}
		
		Item get() {
			switch (kind):
				Item: return item;
				Task: return task;
		}
		
		static VariantItem create(Item item) {
			VariantItem v = new VariantItem();
			v.kind = item.kind;
			switch (item.kind): {
				Item: { 
					v.item = item;
					break;
				}
				Task: {
					v.task = (Task)item;
					break;
				}
			}
			return v;
		}
	};
	
	
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
				// do something with the handler on top
				
				if (addNewItemOnline(item, handler)) {
					// it's bad that user gets feedback eventhough sync is not run
					sync();
				}
			}
            
            taskHandler.post(r);
			
        } else {

            // other wise we are fine doing it in sync
            item.state = ItemState.Local;
            dbRepo.addItem(item);

            // notifications should come from the repo
            // about data change

			runOnUiThread(handler, false);	
            // catch exception let user know if was able to save
        }

    }

    private void updateLocal() {
        ItemsApi api = itemsApiFactory.create();
		
		// get 
		
		// should be processed for polymorphism
		ItemList itemList = api.getItemListGVersion(localVersion);
		
		// save everything to here
		
		// update localVersion
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

        runOnUiThread(handler, false);
    }


    private boolean addNewItemOnline(Item item, Handler handler) {
        ItemsApi api = itemsApiFactory.create();
        // so we have to use fucking if statement and it will happen many many times.
        if (item.getItemKind() == ItemKind.Item) {
            api.addItemOnline(getApiItem(item, Optional.empty())).execute();
        } else {
            api.addTaskOnline(item).execute();
        }
		runOnUiThread(handler, false);
        // in case of exception
        // can actually sort out exceptions to figure out if can do it offline

		return false;

        return true;
    }

    public UUID getRootId() {
        return dbRepo.getRootId();
    }

    public boolean hasRemovedItems() {
		// offline
	}
	
	// user has to handle
    public void addItemList(List<Item> result, Handler handler) {
		// try online.
	}

    public Item getItem(UUID parentId) {
		// offline
	}

    public void updateItem(Item moveItem) {    
		// online first
		// then try offline if local one
	}

	// items has to be onces from the server
	// can we force server involvement???
    private void updateAllItems(List<Item> items) {
		// update is based on where you are.
		// 
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
		// offline
    }

    public Collection<? extends Item> getChildren(UUID parentId) {
        // offline
		return null;
    }

	// check handler for null, add some description for user to post
	// make Handler an optional type
    void runOnUiThread(Handler handler, boolean outcome) {
        taskHandler.post(new Runnable() {
			void run() {
				Message.obtainMsg;
				msg.obj = outcome; // success or not
				handler.postMessage(msg);
			}
		} 
    }


    // conversion methods
}
