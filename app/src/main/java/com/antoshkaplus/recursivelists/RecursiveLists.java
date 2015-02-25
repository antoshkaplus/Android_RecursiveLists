package com.antoshkaplus.recursivelists;

import android.content.Intent;

import java.io.Closeable;
import java.io.IOException;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by antoshkaplus on 2/22/15.
 */
public class RecursiveLists implements Closeable {

    private Dictionary<Integer, Item> items_;
    private Dictionary<Integer, List<Item>> items;
    private static RecursiveLists instance = null;


    public static RecursiveLists getInstance() {
        if (instance == null) {
            instance = new RecursiveLists();
        }
        return instance;
    }

    private RecursiveLists() {

    }

    public Item getItem(int id) {
        return items_.get(id);
    }

    public List<Item> getListItems(int parentId) {
        return items.get(parentId);
    }

    public List<Item> getRoot() {
        return items.get(0);
    }

    public int getRootId() {
        return 0;
    }

    public void remove(Item item) {

    }

    public void removeInner(Item item) {


    }

    public void create(String title,
                int order,
                int parentKey) {


    }

    //
    public void update(Item item) {


    }

    @Override
    public void close() throws IOException {
        // save file
        // or we shouldn't implement this interface
    }

}
