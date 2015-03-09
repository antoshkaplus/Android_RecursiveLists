package com.antoshkaplus.recursivelists;

import com.antoshkaplus.recursivelists.model.Item;

import java.util.List;

/**
 * Created by antoshkaplus on 11/6/14.
 */
public interface DataSet {
    void deleteItem(Item item) throws Exception;
    void deleteChildren(Item item) throws Exception;
    void addItem(Item item) throws Exception;
    Item getItem(int id) throws Exception;
    List<Item> getChildren(int id) throws Exception;
    void updateItem(Item item) throws Exception;

}