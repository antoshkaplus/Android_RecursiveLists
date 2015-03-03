package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * well can think of deleting items better,
 * but user is too slow... creating data
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

    public List<Item> getChildren(int id) throws SQLException {
        return helper.getItemDao().queryBuilder()
                .orderBy(Item.FIELD_NAME_ORDER, true)
                .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                .query();
    }

    public void deleteItem(Item item) throws SQLException {
        helper.getItemDao().delete(item);
    }

    @Override
    public void deleteChildren(Item item) throws Exception {
        DeleteBuilder<Item, Integer> builder = helper.getItemDao().deleteBuilder();
        builder.where()
               .eq(Item.FIELD_NAME_PARENT_ID, item.id);
        builder.delete();
    }

    @Override
    public void addItem(Item item) throws Exception {
        List<Item> items = getChildren(item.parentId);
        items = items.subList(item.order, items.size());
        for (Item i : items) {
            ++i.order;
            updateItem(i);
        }
        helper.getItemDao().create(item);
    }

    public void updateItem(Item item) throws Exception {
        helper.getItemDao().update(item);
    }

    public void updateItems(final List<Item> items) throws Exception {
        final Dao<Item, Integer> dao = helper.getItemDao();
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (Item i : items) {
                    dao.update(i);
                }
                return null;
            }
        });
    }

    public void close() {
        helper.close();
    }
}