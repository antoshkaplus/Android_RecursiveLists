package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.ItemKind;
import com.antoshkaplus.recursivelists.backend.model.Task;

/**
 * Created by antoshkaplus on 4/25/17.
 */

public class VariantItem {
    Item item;
    Task task;

    private VariantItem() {}

    public Item get() {
        if (item != null) return item;
        if (task != null) return task;
        return null;
    }

    public Item getItem() {
        return item;
    }

    public Task getTask() {
        return task;
    }

    public static VariantItem create(Item item) {
        VariantItem v = new VariantItem();
        switch (item.getKind()) {
            case Item:
                v.item = item;
                break;
            case Task:
                v.task = (Task)item;
                break;
            default:
                throw new RuntimeException("Unknown kind");
        }
        return v;
    }
};