package com.antoshkaplus.recursivelists;

import android.content.Context;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.UserItem;
import com.antoshkaplus.recursivelists.model.UserRoot;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
        UserRoot userRoot = dao.queryForId(user);
        if (userRoot == null) {
            userRoot = new UserRoot(user, UUID.randomUUID());
            dao.create(userRoot);
        }
        return userRoot.rootId;
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
        final List<RemovedItem> removedItems = new ArrayList<>();
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
//    public void clear() throws SQLException {
//        // remove removedItems with join of UserItem
//
//        // remove Items with join of UserItem
//
//        // remove UserItem
//
//        // remove UserRoot
//
//        helper.getDao(Item.class).deleteBuilder().delete();
//        helper.getDao(RemovedItem.class).deleteBuilder().delete();
//    }

    public void addItem(Item item) throws Exception {
        helper.getDao(Item.class).create(item);
        helper.getDao(UserItem.class).create(new UserItem(user, item));
    }

    public void addItemList(final List<Item> items) throws Exception {
        // add all of them to UserItem
        final Dao<Item, UUID> dao = helper.getDao(Item.class);
        final Dao<UserItem, Void> userItemDao = helper.getDao(UserItem.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (Item i : items) {
                    dao.create(i);
                    userItemDao.create(new UserItem(user, i));
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

    // here i need to join UserItem table

    public void undoLastRemoval() throws Exception {
        Dao<RemovedItem, Void> dao = helper.getDao(RemovedItem.class);
        QueryBuilder<RemovedItem, Void> b = getRemovedItemQueryBuilder();
        b.orderBy(RemovedItem.FIELD_DELETION_DATE, false).limit((long)1);
        RemovedItem r = b.queryForFirst();

        DeleteBuilder<RemovedItem, Void> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq(RemovedItem.FIELD_ITEM, r.item);
        deleteBuilder.delete();
    }

    private QueryBuilder<RemovedItem, Void> getRemovedItemQueryBuilder() throws SQLException {
        // must do 3 joins to select removed items of particular user
        QueryBuilder<Item, UUID> b = getItemQueryBuilder();
        Dao<RemovedItem, Void> dao = helper.getDao(RemovedItem.class);
        return dao.queryBuilder().join(b);
    }

    private QueryBuilder<Item, UUID> getItemQueryBuilder() throws SQLException {
        Dao<UserItem, Void> userItemDao = helper.getDao(UserItem.class);
        QueryBuilder<UserItem, Void> b = userItemDao.queryBuilder();
        b.where().eq(UserItem.FIELD_NAME_USER, user);

        Dao<Item, UUID> itemDao = helper.getDao(Item.class);
        return itemDao.queryBuilder().join(b);
    }


    public boolean hasRemovedItems() throws SQLException {
        return getRemovedItemQueryBuilder().countOf() > 0;
    }

    public List<Item> getAllItems() throws Exception {
        return getItemQueryBuilder().query();
    }

    public List<RemovedItem> getAllRemovedItems() throws Exception {
        return getRemovedItemQueryBuilder().query();
    }

//    public void init(List<Item> items, final List<RemovedItem> removedItems) throws Exception{
//        clear();
//        // need also initialize rootId
//        addItemList(items);
//        addRemovedItemList(removedItems);
//    }

    public void close() {
        helper.close();
    }
}