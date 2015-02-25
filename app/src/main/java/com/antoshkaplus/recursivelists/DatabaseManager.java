package com.antoshkaplus.recursivelists;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by antoshkaplus on 11/3/14.
 */
public class DatabaseManager implements DataSet {
    private static final String TAG = "DatabaseManager";

    private DatabaseHelper helper;

    public DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public Item getItem(int id) throws Exception {
        return helper.getItemDao().queryForId(id);
    }

    public List<Item> getChildren(Item item) throws SQLException {
        return helper.getItemDao().queryForEq(Item.FIELD_NAME_PARENT_ID, item.id);
    }

    public void deleteItem(Item item) throws SQLException {
        helper.getItemDao().delete(item);
    }

    @Override
    public void deleteChildren(Item item) throws Exception {
        helper.getItemDao()
                .deleteBuilder()
                .where()
                .eq(Item.FIELD_NAME_PARENT_ID, item.id)
                .query();
    }

    @Override
    public void addItem(Item item) throws Exception {
        helper.getItemDao().create(item);
    }

    public void updateItem(Item item) throws SQLException {
        helper.getItemDao().update(item);
    }

    public void close() {
        helper.close();
    }
}