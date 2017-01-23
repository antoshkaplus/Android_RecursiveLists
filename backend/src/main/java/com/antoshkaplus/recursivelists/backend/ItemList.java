package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 9/5/16.
 */
public class ItemList {

    private List<Item> items = new ArrayList<>();


    public ItemList() {}

    public ItemList(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}


