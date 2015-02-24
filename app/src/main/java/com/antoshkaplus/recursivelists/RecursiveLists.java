package com.antoshkaplus.recursivelists;

import java.io.Closeable;
import java.io.IOException;
import java.util.Dictionary;
import java.util.List;

/**
 * Created by antoshkaplus on 2/22/15.
 */
public class RecursiveLists implements Closeable {

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

    public List<Item> getListItems(int key) {
        return items.get(key);
    }

    public List<Item> getRoot() {
        return items.get(0);
    }

    public int getRootId() {
        return 0;
    }

    public void remove(Item item) {

    }

    public void create(String title,
                int order,
                int parentKey) {


    }

    public void save(Item item) {


    }

    @Override
    public void close() throws IOException {
        // save file
        // or we shouldn't implement this interface
    }

}
