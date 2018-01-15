package com.antoshkaplus.recursivelists.data;

import com.antoshkaplus.recursivelists.backend.itemsApi.model.VariantItem;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.Task;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;

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


    static Item toLocalItem(VariantItem item) {
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

    // different beast
    static VariantItem toRemoteItem(Item item, RemovedItem removedItem) {
        VariantItem vItem = new VariantItem();
        switch (item.getItemKind()) {
            case Item:
                com.antoshkaplus.recursivelists.backend.itemsApi.model.Item rItem = new com.antoshkaplus.recursivelists.backend.itemsApi.model.Item();
                rItem.setTitle(item.title);
                rItem.setUuid(item.id.toString());
                rItem.setParentUuid(item.parentId.toString());
                vItem.setItem(rItem);
                break;
            case Task:
                Task task = (Task)item;
                com.antoshkaplus.recursivelists.backend.itemsApi.model.Task rTask = new com.antoshkaplus.recursivelists.backend.itemsApi.model.Task();
                rTask.setTitle(item.title);
                rTask.setUuid(item.id.toString());
                rTask.setParentUuid(item.parentId.toString());
                vItem.setTask(rTask);
                break;
            default:
                throw new RuntimeException("Unknown ItemKind");
        }
        return vItem;
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
