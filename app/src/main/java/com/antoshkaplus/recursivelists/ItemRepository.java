package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.UserRoot;
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
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * well can think of deleting items better,
 * but user is too slow... creating data
 */
public class ItemRepository {
    private static final String TAG = "ItemRepository";

    private DatabaseHelper helper;
    private String user;

    public ItemRepository(Context ctx, String user) {
        helper = new DatabaseHelper(ctx);
        this.user = user;
    }

    public UUID getRootId() throws SQLException {
        Dao<UserRoot, String> dao = helper.getDao(UserRoot.class);
        return dao.queryForId(user).rootId;
    }

    public Item getItem(UUID id) throws Exception {
        Dao<Item, UUID> dao = helper.getDao(Item.class);
        return dao.queryForId(id);
    }

    public List<Item> getChildren(UUID id) throws SQLException {
        return helper.getDao(Item.class).queryBuilder()
                .orderBy(Item.FIELD_NAME_ORDER, true)
                .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                .and()
                .notIn(Item.FIELD_NAME_ID, helper.getDao(RemovedItem.class).queryBuilder().selectColumns(RemovedItem.FIELD_ITEM))
                .query();
    }

    public int getChildrenCount(UUID id) throws SQLException {
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

    // clearing every table from anything of particular userId
    public void clear() throws SQLException {
        // remove removedItems with join of UserItem

        // remove Items with join of UserItem

        // remove UserItem

        // remove UserRoot

        helper.getDao(Item.class).deleteBuilder().delete();
        helper.getDao(RemovedItem.class).deleteBuilder().delete();
    }

    public void addItem(Item item) throws Exception {
        // add item to UserItem
        List<Item> items = getChildren(item.parentId);
        items = items.subList(item.order, items.size());
        for (Item i : items) {
            ++i.order;
            updateItem(i);
        }
        helper.getDao(Item.class).create(item);
    }

    public void addItemList(final List<Item> items) throws Exception {
        // add all of them to UserItem
        final Dao<Item, UUID> dao = helper.getDao(Item.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (Item i : items) {
                    dao.create(i);
                }
                return null;
            }
        });
    }

    public void addRemovedItemList(final List<RemovedItem> removedItems) throws Exception {
        final Dao<RemovedItem, Integer> dao = helper.getDao(RemovedItem.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (RemovedItem i : removedItems) {
                    dao.create(i);
                }
                return null;
            }
        });
    }

    // should not change id by any means
    public void updateItem(Item item) throws Exception {
        helper.getDao(Item.class).update(item);
    }

    public void updateAllItems(final List<Item> items) throws Exception {
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

    public List<Item> getAllItems() throws Exception {
        return helper.getDao(Item.class).queryForAll();
    }

    public List<RemovedItem> getAllRemovedItems() throws Exception {
        return helper.getDao(RemovedItem.class).queryForAll();
    }

    public void init(List<Item> items, final List<RemovedItem> removedItems) throws Exception{
        clear();
        // need also initialize rootId
        addItemList(items);
        addRemovedItemList(removedItems);
    }

    public void close() {
        helper.close();
    }
}