package com.antoshkaplus.recursivelists;

import com.j256.ormlite.dao.Dao;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 11/6/14.
 */
public interface DataSet {
    void deleteItem(Item item) throws Exception;
    void deleteChildren(Item item) throws Exception;
    void addItem(Item item) throws Exception;
    Item getItem(int id) throws Exception;
    List<Item> getChildren(Item item) throws Exception;
    void updateItem(Item item) throws Exception;
}