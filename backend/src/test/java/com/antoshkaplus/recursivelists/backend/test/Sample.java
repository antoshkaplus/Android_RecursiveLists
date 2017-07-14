package com.antoshkaplus.recursivelists.backend.test;

import com.antoshkaplus.recursivelists.backend.Gtask;
import com.antoshkaplus.recursivelists.backend.GtaskList;
import com.antoshkaplus.recursivelists.backend.ItemsEndpoint;
import com.antoshkaplus.recursivelists.backend.VariantItem;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.google.appengine.api.users.User;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by antoshkaplus on 2/18/17.
 */

public class Sample {

    String filename;
    ItemsEndpoint endpoint;
    User user;

    Sample(String filename) {
        this.filename = filename;
    }

    private void handle(JSONObject obj, String parentUuid) {

        String kind = (String)obj.get("kind");
        String uuid = null;
        if (kind == null || kind.equals("Item")) {
            Item item = initItem(obj);
            item.setParentUuid(parentUuid);
            uuid = item.getUuid();
            endpoint.addVariantItem(VariantItem.create(item), user);
        } else {
            Task task = initTask(obj);
            task.setParentUuid(parentUuid);
            uuid = task.getUuid();
            endpoint.addVariantItem(VariantItem.create(task), user);
        }
        JSONArray arr = (JSONArray) obj.get("children");
        if (arr == null) return;
        for (Object a : arr) {
            handle((JSONObject) a, uuid);
        }
    }

    private Item initItem(JSONObject obj) {
        Item item = new Item();
        initItem(obj, item);
        return item;
    }

    private Task initTask(JSONObject obj) {
        Task task = new Task();
        initItem(obj, task);

        Boolean completed = (Boolean) obj.get("completed");
        if (completed != null && completed) task.setCompleteDate(new Date());

        return task;
    }

    private void initItem(JSONObject obj, Item item) {
        String t = (String) obj.get("title");

        item.setTitle(t);
        item.setUuid(t);
        item.setCreateDate(new Date());
    }

    private Gtask initGtask(JSONObject obj) {
        Gtask g = new Gtask();
        String t = (String) obj.get("title");
        g.setId(t);
        g.setTitle(t);
        g.setUpdated(new Date());
        return g;
    }

    public void apply(ItemsEndpoint endpoint, User user) {
        this.endpoint = endpoint;
        this.user = user;
        String rootUuid = endpoint.getRootUuid(user).getUuid();

        JSONParser parser = new JSONParser();
        JSONObject obj;
        try {
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));
            obj = (JSONObject) parser.parse(new FileReader(filename));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Sample file " + filename + " parsing problem");
        }
        JSONArray arr = (JSONArray)obj.get("items");
        for (Object a : arr) {
            handle((JSONObject) a, null);
        }
        arr = (JSONArray)obj.get("gtasks");
        if (arr != null) {
            for (Object a : arr) {
                JSONObject j = (JSONObject) a;
                Gtask g = initGtask(j);
                String parentUuid = (String) j.get("parentUuid");
                GtaskList list = new GtaskList(Arrays.asList(g), parentUuid);
                endpoint.updateGtaskList(list, user);
            }
        }
    }
}
