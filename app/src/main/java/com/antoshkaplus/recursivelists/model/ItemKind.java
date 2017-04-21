package com.antoshkaplus.recursivelists.model;

import android.content.Context;

/**
 * Created by antoshkaplus on 3/5/17.
 */

// this should not be an enum
// it's like a table where you keep each id and type of it

public enum ItemKind {
    Item,
    Task;

    // label is what we have in resource array in item_kind_array
    static public ItemKind fromResourceIndex(int index) {
        switch (index) {
            case 0:
                return ItemKind.Item;
            case 1:
                return ItemKind.Task;
        }
        throw new RuntimeException("Unknown item kind id");
    }


}
