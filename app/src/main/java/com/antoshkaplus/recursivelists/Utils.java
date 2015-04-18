package com.antoshkaplus.recursivelists;

import android.content.res.Resources;
import android.util.TypedValue;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.google.api.client.util.DateTime;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by antoshkaplus on 10/30/14.
 */
public final class Utils {

    public static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static JSONObject readDefaultData(InputStream inputStream) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return new JSONObject(total.toString());
    }

    public static com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item toBackendItem(Item item, DateTime dateTime) {
        com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item apiItem = new com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item();
        apiItem.setId(item.id.toString());
        apiItem.setTitle(item.title);
        apiItem.setOrder(item.order);
        apiItem.setParentId(item.parentId.toString());
        apiItem.setDeletionDate(dateTime);
        return apiItem;
    }

    public static List<com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item> toBackendItems(List<Item> items, List<RemovedItem> removedItems) {
        ArrayList<com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item> apiItems = new ArrayList<>();
        HashMap<UUID, DateTime> removed = new HashMap<>();
        for (RemovedItem i : removedItems) {
            removed.put(i.item.id, new DateTime(i.deletionDate.getTime()));
        }
        for (Item i : items) {
            apiItems.add(toBackendItem(i, removed.get(i.id)));
        }
        return apiItems;
    }

    public static Item toClientItem(com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item item) {
        Item clientItem = new Item(item.getTitle(), item.getOrder(), java.util.UUID.fromString(item.getParentId()));
        clientItem.id = java.util.UUID.fromString(item.getId());
        return clientItem;
    }


    public static RemovedItem toClientRemovedItem(com.antoshkaplus.recursivelists.backend.userItemsApi.model.RemovedItem removedItem) {
        Item item = new Item();
        item.id = java.util.UUID.fromString(removedItem.getItem().getId());
        return new RemovedItem(item, new Date(removedItem.getDeletionDate().getValue()));
    }
}
