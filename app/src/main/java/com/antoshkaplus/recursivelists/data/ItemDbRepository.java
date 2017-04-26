package com.antoshkaplus.recursivelists.data;

import android.content.Context;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.Task;
import com.antoshkaplus.recursivelists.model.UserItem;
import com.antoshkaplus.recursivelists.model.UserRoot;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
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
 *
 *
 */
public class ItemDbRepository {
    private static final String TAG = "ItemRepository";

    private DatabaseHelper helper;
    private String user;

    public ItemDbRepository(Context ctx, String user) {
        helper = new DatabaseHelper(ctx);
        this.user = user;
    }

    public ItemDbRepository(DatabaseHelper helper, String user) {
        this.helper = helper;
        this.user = user;
    }

    public UUID getRootId() {
        RuntimeExceptionDao<UserRoot, String> dao = helper.getRuntimeExceptionDao(UserRoot.class);
        UserRoot userRoot = dao.queryForId(user);
        if (userRoot == null) {
            userRoot = new UserRoot(user, UUID.randomUUID());
            dao.create(userRoot);
        }
        return userRoot.rootId;
    }

    // we don't care if it was removed
    // we return it to the user
    public Item getItem(UUID id) {
        RuntimeExceptionDao<Item, UUID> dao = helper.getRuntimeExceptionDao(Item.class);
        Item item = dao.queryForId(id);
        if (item == null) {
            RuntimeExceptionDao<Task, UUID> taskDao = helper.getRuntimeExceptionDao(Task.class);
            item = taskDao.queryForId(id);
        }
        return item;
    }

    public List<Item> getChildren(UUID id) {
        List<Item> items = getChildren(id, Item.class);
        items.addAll(getChildren(id, Task.class));
        items.sort((i_1, i_2) -> i_1.order - i_2.order);
        return items;
    }

    private <T extends Item> List<T> getChildren(UUID id, Class<T> cls) {
        try {
            return helper.getDao(cls).queryBuilder()
                    .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                    .and()
                    .notIn(Item.FIELD_NAME_ID, helper.getRuntimeExceptionDao(RemovedItem.class).queryBuilder().selectColumns(RemovedItem.FIELD_ITEM_ID))
                    .query();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int getChildrenCount(UUID id) {
        return getChildrenCount(id, Item.class) + getChildrenCount(id, Task.class);
    }

    private <T> int getChildrenCount(UUID id, Class<T> cls) {
        try {
            return (int) helper.getDao(cls).queryBuilder()
                    .where().eq(Item.FIELD_NAME_PARENT_ID, id)
                    .and()
                    .notIn(Item.FIELD_NAME_ID, helper.getDao(RemovedItem.class).queryBuilder().selectColumns(RemovedItem.FIELD_ITEM_ID))
                    .countOf();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
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


    // we are not going to check buisiness rules, but parent item has to be there
    // otherwise element aren't visible at all

    // it has to be in transaction!!!
    public void addItem(Item item) {
        try {
            UUID root = getRootId();
            if (!item.parentId.equals(root)) {
                long itemCount = helper.getDao(Item.class).queryBuilder().where().eq(Item.FIELD_NAME_PARENT_ID, item.parentId).countOf();
                long taskCount = helper.getDao(Task.class).queryBuilder().where().eq(Item.FIELD_NAME_PARENT_ID, item.parentId).countOf();
                if (itemCount + taskCount == 0) throw new RuntimeException("parent doesn't exists");
            }

            if (item.getItemKind() == ItemKind.Task) {
                Dao<Item, UUID> i = helper.getDao(Item.class);
                if (i.queryForId(item.id) != null)
                    throw new RuntimeException("Task can't be created, item is already exists");
                // don't forget about subtask logic
                helper.getDao(Task.class).create((Task) item);
            }
            if (item.getItemKind() == ItemKind.Item) {
                Dao<Task, UUID> i = helper.getDao(Task.class);
                if (i.queryForId(item.id) != null)
                    throw new RuntimeException("Item can't be created, task is already exists");
                helper.getDao(Item.class).create(item);
            }
            helper.getDao(UserItem.class).create(new UserItem(user, item));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addItemList(final List<Item> items) {
        // add all of them to UserItem
        final RuntimeExceptionDao<Item, UUID> dao = helper.getRuntimeExceptionDao(Item.class);
        dao.callBatchTasks(() -> {
            for (Item i : items) {
                addItem(i);
            }
            return null;
        });
    }

    // could go with just remove items
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
        if (item.getItemKind() == ItemKind.Task) {
            helper.getDao(Task.class).update((Task)item);
        }
        if (item.getItemKind() == ItemKind.Task) {
            helper.getDao(Item.class).update(item);
        }
    }

    public void updateAllItems(final List<Item> items) throws Exception {
        final Dao<Item, Integer> dao = helper.getDao(Item.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (Item i : items) {
                    updateItem(i);
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
        deleteBuilder.where().eq(RemovedItem.FIELD_ITEM_ID, r.itemId);
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


    public boolean hasRemovedItems() {
        try {
            return getRemovedItemQueryBuilder().countOf() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
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

    public int getLastSyncVersion() {
        RuntimeExceptionDao<UserRoot, String> dao = helper.getRuntimeExceptionDao(UserRoot.class);
        UserRoot userRoot = dao.queryForId(user);
        if (userRoot == null) {
            userRoot = new UserRoot(user, UUID.randomUUID());
            dao.create(userRoot);
        }
        return userRoot.lastSyncVersion;
    }
}