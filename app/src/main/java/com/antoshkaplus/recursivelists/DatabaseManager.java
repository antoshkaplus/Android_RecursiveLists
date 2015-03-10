package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.ColumnArg;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
        Dao<Item, Integer> dao = helper.getDao(Item.class);
        return dao.queryForId(id);
    }

    public List<Item> getChildren(int id) throws SQLException {
        return helper.getDao(Item.class).queryBuilder()
                .orderBy(Item.FIELD_NAME_ORDER, true)
                .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                .and()
                .notIn(Item.FIELD_NAME_ID, helper.getDao(RemovedItem.class).queryBuilder().selectColumns(RemovedItem.FIELD_ITEM))
                .query();
    }

    public int getChildrenCount(int id) throws SQLException {
        return (int)helper.getDao(Item.class)
                .queryBuilder()
                .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                .countOf();
    }

    public void deleteItem(Item item) throws SQLException {
        Dao<RemovedItem, Integer> dao = helper.getDao(RemovedItem.class);
        RemovedItem removedItem = new RemovedItem(item);
        dao.create(removedItem);
    }

    @Override
    public void deleteChildren(Item item) throws Exception {
        List<Item> items = getChildren(item.id);
        final List<RemovedItem> removedItems = new ArrayList<RemovedItem>();
        Date date = new Date();
        for (Item i : items) {
            removedItems.add(new RemovedItem(i, date));
        }
        final Dao<RemovedItem, Integer> dao = helper.getDao(RemovedItem.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (RemovedItem r : removedItems) {
                    dao.create(r);
                }
                return null;
            }
        });
    }

    public void clear() throws SQLException {
        helper.getDao(Item.class).deleteBuilder().delete();
        helper.getDao(RemovedItem.class).deleteBuilder().delete();
    }

    @Override
    public void addItem(Item item) throws Exception {
        List<Item> items = getChildren(item.parentId);
        items = items.subList(item.order, items.size());
        for (Item i : items) {
            ++i.order;
            updateItem(i);
        }
        helper.getDao(Item.class).create(item);
    }

    public void updateItem(Item item) throws Exception {
        helper.getDao(Item.class).update(item);
    }

    public void updateItems(final List<Item> items) throws Exception {
        final Dao<Item, Integer> dao = helper.getDao(Item.class);
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

    public void undoLastRemoval() throws Exception {
        Dao<RemovedItem, Integer> dao = helper.getDao(RemovedItem.class);
        String[] result = dao.queryBuilder().selectRaw("MAX(" + RemovedItem.FIELD_DELETION_DATE + ")").queryRawFirst();
        String[] name = new String[] { RemovedItem.FIELD_DELETION_DATE };
        RemovedItem i = dao.getRawRowMapper().mapRow(name, result);
        DeleteBuilder<RemovedItem, Integer> builder = dao.deleteBuilder();
        builder.where().eq(RemovedItem.FIELD_DELETION_DATE, i.deletionDate);
        builder.delete();
    }

    public boolean hasRemovedItems() throws SQLException {
        return helper.getDao(RemovedItem.class).countOf() > 0;
    }

    public void close() {
        helper.close();
    }
}