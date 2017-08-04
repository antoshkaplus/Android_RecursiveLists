package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.bee.backend.ResourceDate;
import com.antoshkaplus.recursivelists.backend.bean.IdList;
import com.antoshkaplus.recursivelists.backend.bean.Uuid;
import com.antoshkaplus.recursivelists.backend.bean.VariantItem;
import com.antoshkaplus.recursivelists.backend.bean.VariantItemList;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.test.*;
import com.antoshkaplus.recursivelists.backend.test.Util;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.AsyncCacheFilter;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.antoshkaplus.recursivelists.backend.test.Util.getSample;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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

        VariantItemList list = endpoint.getChildrenItems(uuid.getUuid(), user);
        assertTrue(list.size() == 0);
    }

    @Test
    public void getChildrenItems_1() {
        Sample s = getSample(1);
        s.apply(endpoint, user);

        String rootUuid = endpoint.getRootUuid(user).getUuid();

        VariantItemList list = endpoint.getChildrenItems(rootUuid, user);
        assertTrue(list.size() == 3);

        list = endpoint.getChildrenItems("a", user);
        assertTrue(list.size() == 2);

        list = endpoint.getChildrenItems("b", user);
        assertTrue(list.size() == 0);
    }

    @Test
    public void getItems_Empty() {
        VariantItemList list = endpoint.getItems(user);
        assertEquals(list.size(), 0);
    }

    @Test
    public void getItems_1() {
        getSample(1).apply(endpoint, user);
        VariantItemList list = endpoint.getItems(user);
        assertEquals(list.size(), 5);
    }

    @Test
    public void getRootUuid() {
        String root = endpoint.getRootUuid(user).getUuid();
        Sample s = getSample(1);
        s.apply(endpoint, user);
        VariantItemList list = endpoint.getChildrenItems(root, user);
        for (Item item : list.convertToItems()) {
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
        getSample(2).apply(endpoint, user);
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
        getSample(2).apply(endpoint, user);
        com.antoshkaplus.recursivelists.backend.test.Util.getScenario(1).apply(endpoint, user);
        // TODO disable
    }

    @Test
    public void getItemsByUuid() {
        getSample(4).apply(endpoint, user);
        IdList ids = new IdList(Arrays.asList("a_i", "a_t", "d_i", "d_t"));
        VariantItemList itemList = endpoint.getItemsByUuid(ids ,user);
        assertEquals(4, itemList.size());
        Iterator<Item> items = itemList.convertToItems().iterator();
        Iterator<String> idsIt = ids.getIds().iterator();
        while (items.hasNext()) {
            assertEquals(items.next().getUuid(), idsIt.next());
        }
    }

    @Test
    public void getItemsByUuid_NonExistingId() {
        getSample(4).apply(endpoint, user);
        IdList ids = new IdList(Arrays.asList("a_i", "a_t", "t_i", "d_t"));
        VariantItemList itemList = endpoint.getItemsByUuid(ids ,user);
        assertEquals(4, itemList.size());
        assertNull(itemList.getVariantItems().get(2));
    }

    @Test
    public void getItemsByGtaskId() {
    }

    @Test
    public void addItemOnline_VersionIncrease() {
        assertEquals(0, endpoint.getDbVersion(user).getValue());
        getSample(3).apply(endpoint, user);
        char ch = 'a';
        for (int i = 0; i < 4; ++i) {
            Item item = endpoint.getItem("" + (char)(ch+i), user).get();
            assertEquals(i+1, item.getDbVersion());
        }
        assertEquals(4, endpoint.getDbVersion(user).getValue());
    }

    @Test
    public void addTaskOnline() {
    }

    @Test
    public void removeItem() {
        getSample(1).apply(endpoint, user);
        assertEquals(2, Util.countEnabled(endpoint.getChildrenItems("a", user)));
        endpoint.removeVariantItem(VariantItem.create(Util.itemWithUuid("aa")), user);
        assertEquals(1, Util.countEnabled(endpoint.getChildrenItems("a", user)));
    }

    @Test
    public void removeTask_DecChildren() {
        getSample(5).apply(endpoint, user);
        assertEquals(3, Util.countEnabled(endpoint.getChildrenItems("a_t", user)));
        endpoint.removeVariantItem(VariantItem.create(Util.taskWithUuid("d_t")), user);
        assertEquals(2, Util.countEnabled(endpoint.getChildrenItems("a_t", user)));
    }

    @Test
    public void removeTask_StaysComplete() {
        getSample(5).apply(endpoint, user);
        assertEquals(true, endpoint.getItem("a_t", user).getTask().isCompleted());
        endpoint.removeVariantItem(VariantItem.create(Util.taskWithUuid("d_t")), user);
        assertEquals(true, endpoint.getItem("a_t", user).getTask().isCompleted());
    }

    @Test
    public void removeTask_BecomesComplete() {
        getSample(5).apply(endpoint, user);
        assertEquals(false, endpoint.getItem("e_t", user).getTask().isCompleted());
        endpoint.removeVariantItem(VariantItem.create(Util.taskWithUuid("f_t")), user);
        assertEquals(true, endpoint.getItem("e_t", user).getTask().isCompleted());
    }

    @Test
    public void moveItem() {
    }

    @Test
    public void moveTask() {
    }

    @Test
    public void completeTask() {
        getSample(3).apply(endpoint, user);
        com.antoshkaplus.recursivelists.backend.test.Util.getScenario(2).apply(endpoint, user);
    }
}