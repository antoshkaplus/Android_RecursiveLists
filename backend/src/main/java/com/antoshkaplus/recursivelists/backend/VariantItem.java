package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.ItemKind;
import com.antoshkaplus.recursivelists.backend.model.Task;

/**
 * Created by antoshkaplus on 4/25/17.
 */

class VariantItem {
    Item item;
    Task task;

    ItemKind kind;

    private VariantItem() {}

    Item get() {
        switch (kind) {
            case Item:
                return item;
            case Task:
                return task;
            default:
                throw new RuntimeException("Unknown kind");
        }
    }

    static VariantItem create(Item item) {
        VariantItem v = new VariantItem();
        v.kind = item.getKind();
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