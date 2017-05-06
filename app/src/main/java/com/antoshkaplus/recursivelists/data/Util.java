package com.antoshkaplus.recursivelists.data;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.RemovedItem;

import java.util.Optional;

/**
 * Created by antoshkaplus on 4/23/17.
 */

class Util {
    // may not be needed
    static com.antoshkaplus.recursivelists.backend.itemsApi.model.Item getApiItem(Item item, Optional<RemovedItem> removedItem) {
        com.antoshkaplus.recursivelists.backend.itemsApi.model.Item apiItem = new com.antoshkaplus.recursivelists.backend.itemsApi.model.Item();
        if (!removedItem.isPresent()) {
            apiItem.setDisabled(removedItem.get().deletionDate == null);
        }
        apiItem.setKind(getApiItemKind(item.getItemKind()));
        apiItem.setParentUuid(item.parentId.toString());
        apiItem.setTitle(item.title);
        apiItem.setUuid(item.id.toString());
        return apiItem;
    }

    static String getApiItemKind(ItemKind kind) {
        switch (kind) {
            case Item:
                return "Item";
            case Task:
                return "Task";
            default:
                throw new RuntimeException("Unknown ItemKind");
        }
    }


//    public static com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item toBackendItem(Item item, DateTime dateTime) {
//        com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item apiItem = new com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item();
//        apiItem.setId(item.id.toString());
//        apiItem.setTitle(item.title);
//        apiItem.setOrder(item.order);
//        apiItem.setParentId(item.parentId.toString());
//        apiItem.setDeletionDate(dateTime);
//        return apiItem;
//    }
//
//    public static List<com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item> toBackendItems(List<Item> items, List<RemovedItem> removedItems) {
//        ArrayList<com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item> apiItems = new ArrayList<>();
//        HashMap<UUID, DateTime> removed = new HashMap<>();
//        for (RemovedItem i : removedItems) {
//            removed.put(i.item.id, new DateTime(i.deletionDate.getTime()));
//        }
//        for (Item i : items) {
//            apiItems.add(toBackendItem(i, removed.get(i.id)));
//        }
//        return apiItems;
//    }
//
//    public static Item toClientItem(com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item item) {
//        Item clientItem = new Item(item.getTitle(), item.getOrder(), java.util.UUID.fromString(item.getParentId()));
//        clientItem.id = java.util.UUID.fromString(item.getId());
//        return clientItem;
//    }
}
