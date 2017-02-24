package com.antoshkaplus.recursivelists.backend.test;

import com.antoshkaplus.bee.backend.ResourceDate;
import com.antoshkaplus.recursivelists.backend.IdList;
import com.antoshkaplus.recursivelists.backend.ItemList;
import com.antoshkaplus.recursivelists.backend.ItemsEndpoint;
import com.antoshkaplus.recursivelists.backend.Uuid;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class BackendTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(1));

    private Closeable session;
    private User user;
    private ItemsEndpoint endpoint;

    @Before
    public void setUp() {
        this.user = new User("example@example.com", "example.com");
        this.session = ObjectifyService.begin();
        this.helper.setUp();
        this.endpoint = new ItemsEndpoint();
    }

    @After
    public void tearDown() {
        AsyncCacheFilter.complete();
        this.session.close();
        this.helper.tearDown();
    }

    @Test
    public void getChildrenItems_Empty() {
        Uuid uuid = endpoint.getRootUuid(user);

        ItemList list = endpoint.getChildrenItems(uuid.getUuid(), user);
        assertTrue(list.getItems().size() == 0);
    }

    @Test
    public void getChildrenItems_1() {
        Sample s = Util.getSample(1);
        s.apply(endpoint, user);

        String rootUuid = endpoint.getRootUuid(user).getUuid();

        ItemList list = endpoint.getChildrenItems(rootUuid, user);
        assertTrue(list.getItems().size() == 3);

        list = endpoint.getChildrenItems("a", user);
        assertTrue(list.getItems().size() == 2);

        list = endpoint.getChildrenItems("b", user);
        assertTrue(list.getItems().size() == 0);
    }

    @Test
    public void getItems_Empty() {
        ItemList list = endpoint.getItems(user);
        assertEquals(list.getItems().size(), 0);
    }

    @Test
    public void getItems_1() {
        Util.getSample(1).apply(endpoint, user);
        ItemList list = endpoint.getItems(user);
        assertEquals(list.getItems().size(), 5);
    }

    @Test
    public void getRootUuid() {
        String root = endpoint.getRootUuid(user).getUuid();
        Sample s = Util.getSample(1);
        s.apply(endpoint, user);
        ItemList list = endpoint.getChildrenItems(root, user);
        for (Item item : list.getItems()) {
            assertEquals(item.getParentUuid(), root);
        }
    }

    @Test
    public void gtaskLastUpdate() {
        Date startDate = new Date(0);
        ResourceDate rDate_1 = endpoint.getGtaskLastUpdate(user);
        assertEquals(rDate_1.getValue(), startDate);
        rDate_1.setValue(new Date());
        endpoint.updateGtaskLastUpdate(rDate_1, user);
        ResourceDate rDate_2 = endpoint.getGtaskLastUpdate(user);
        assertEquals(rDate_1.getValue(), rDate_2.getValue());
        assertNotEquals(rDate_1.getValue(), new Date());
    }

    @Test
    public void checkGtaskIdPresent() {
        Util.getSample(2).apply(endpoint, user);
        List<String> ids = Arrays.asList("gt_a", "gt_b", "a_gt_a", "a_gt_b");
        IdList idList = new IdList(ids);
        IdList idsRes = endpoint.checkGtaskIdPresent(idList, user);
        for (String id : idsRes.getIds()) {
            assertNotNull(id);
        }
        idList.setIds(Arrays.asList("gt", "a"));
        idList = endpoint.checkGtaskIdPresent(idList, user);
        assertNull(idList.getIds().get(0));
        assertNull(idList.getIds().get(1));
    }

    @Test
    public void updateGtaskList() {
        Util.getSample(2).apply(endpoint, user);
        Util.getScenario(1).apply(endpoint, user);
        // TODO disable
    }

    @Test
    public void getItemsByUuid() {
    }

    @Test
    public void getItemsByGtaskId() {
    }

    @Test
    public void addItemOnline() {
    }

    @Test
    public void addTaskOnline() {
    }

    @Test
    public void removeTask() {
    }

    @Test
    public void moveItem() {
    }

    @Test
    public void moveTask() {
    }

    @Test
    public void completeTask() {
        Util.getSample(3).apply(endpoint, user);
        Util.getScenario(2).apply(endpoint, user);
    }
}