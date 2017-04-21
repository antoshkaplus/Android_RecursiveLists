package com.antoshkaplus.recursivelists.db;


import android.support.test.espresso.core.deps.guava.util.concurrent.ExecutionError;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;
import com.antoshkaplus.recursivelists.model.Task;
import com.antoshkaplus.recursivelists.model.UserItem;
import com.antoshkaplus.recursivelists.model.UserRoot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;

import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by antoshkaplus on 3/10/17.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ItemRepositoryTest {

    ItemRepository repo;
    DatabaseHelper dbHelper;
    static final String TEST_DB_NAME = "test.db";



    @Before
    public void setUp() throws Exception {
        getTargetContext().deleteDatabase(TEST_DB_NAME);
        dbHelper = new DatabaseHelper(getTargetContext(), TEST_DB_NAME);
        repo = new ItemRepository(dbHelper, "example@gmail.com");

        InputStream input = getContext().getResources().openRawResource(com.antoshkaplus.recursivelists.test.R.raw.test_data);

        // this should help when we start dealing with moderate hierarchies.
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode node = mapper.readTree(input);
//        JsonNode item_1 = node.get("item_1");
//
//        Item mm = mapper.treeToValue(item_1, Item.class);
//        String s = mm.title;
    }

    @After
    public void tearDown() throws Exception {
        dbHelper.close();
    }

    @Test(expected = Exception.class)
    public void addItem_NoTitle() throws Exception {
        repo.addItem(new Item());
    }

    @Test(expected = Exception.class)
    public void addItem_NoParent() throws Exception {
        repo.addItem(new Item("hello", 0, null));
    }

    @Test(expected = RuntimeException.class)
    public void addItem_RandomParent() throws Exception {
        repo.addItem(new Item("hello", 0, UUID.randomUUID()));
    }

    @Test
    public void addItem_OK() throws Exception {
        repo.addItem(new Item("hello", 0, repo.getRootId()));
    }

    @Test
    public void addTask_OK() throws Exception {
        Task task = new Task("hello", 0, repo.getRootId());
        repo.addItem(task);
    }

    @Test(expected = SQLException.class)
    public void addItem_sameIdItemItem() throws Exception {
        Item i_1 = new Item("h_1", 0, repo.getRootId());
        Item i_2 = new Item("h_2", 0, repo.getRootId());
        i_2.id = i_1.id;
        repo.addItem(i_1);
        repo.addItem(i_2);
    }

    @Test(expected = RuntimeException.class)
    public void addItem_sameIdItemTask() throws Exception {
        Item i_1 = new Item("h_1", 0, repo.getRootId());
        Task i_2 = new Task("h_2", 0, repo.getRootId());
        i_2.id = i_1.id;
        repo.addItem(i_1);
        repo.addItem(i_2);
    }

    @Test
    public void getItem_Root() throws Exception {
        UUID rootId = repo.getRootId();
        assertNull(repo.getItem(rootId));
    }

    @Test
    public void getItem_nonExistent() throws Exception {
        assertNull(repo.getItem(UUID.randomUUID()));
    }

    @Test
    public void getItem_OK() throws Exception {
        Item item = new Item("hello", 0, repo.getRootId());
        repo.addItem(item);
        assertEquals(repo.getItem(item.id).getItemKind(), ItemKind.Item);
        Task task = new Task("hello", 0, repo.getRootId());
        repo.addItem(task);
        assertEquals(repo.getItem(task.id).getItemKind(), ItemKind.Task);
    }

    @Test
    public void getChildren() throws Exception {
        // here we have problems with getting children
    }

    @Test
    public void getChildrenCount() throws Exception {
        // here we just count children. nothing special
    }

    @Test
    public void deleteItem() throws Exception {
        UUID rootId = repo.getRootId();
        Item item = new Item("h_1", 0, rootId);
        Task task = new Task("h_2", 0, rootId);
        repo.addItem(item);
        repo.addItem(task);
        assertEquals(repo.getChildrenCount(rootId), 2);

        repo.deleteItem(item);
        repo.deleteItem(task);
        assertEquals(repo.getChildrenCount(rootId), 0);
    }

    @Test
    public void deleteChildren() throws Exception {
        // hope easy. need more data
    }
}
