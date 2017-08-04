package com.antoshkaplus.recursivelists.backend.test;

import java.util.Collections;

import com.antoshkaplus.recursivelists.backend.*;
import com.antoshkaplus.recursivelists.backend.bean.Gtask;
import com.antoshkaplus.recursivelists.backend.bean.GtaskList;
import com.antoshkaplus.recursivelists.backend.bean.IdList;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.google.appengine.api.users.User;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by antoshkaplus on 2/20/17.
 */

public class Scenario {

    String filename;
    ItemsEndpoint endpoint;
    User user;

    Scenario(String filename) {
        this.filename = filename;
    }

    public void apply(ItemsEndpoint endpoint, User user) {
        this.endpoint = endpoint;
        this.user = user;

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
        JSONArray arr = (JSONArray)obj.get("actions");
        for (Object a : arr) {
            JSONObject ja = (JSONObject)a;
            String type = (String)ja.get("type");
            switch (type) {
                case "update":
                    handleUpdate(ja);
                    break;
                case "check":
                    handleCheck(ja);
                    break;
                case "complete":
                    handleComplete(ja);
                    break;
                case "move":
                    handleMove(ja);
                    break;
                default:
                    throw new RuntimeException("Action type is not supported");
            }
        }
    }

    private void handleMove(JSONObject obj) {
        String kind = (String)obj.get("kind");
        if (kind.equals("task")) {
            for (Object t : (JSONArray)obj.get("content")) {
                JSONObject jt = (JSONObject) t;
                String uuid = (String) jt.get("uuid");
                String parentUuid = (String) jt.get("parentUuid");
                endpoint.moveTask(uuid, parentUuid, user);
            }
        }
    }

    private void handleComplete(JSONObject obj) {
        String kind = (String)obj.get("kind");
        if (kind.equals("task")) {
            for (Object t : (JSONArray)obj.get("content")) {
                JSONObject jt = (JSONObject) t;
                String uuid = (String) jt.get("uuid");
                Boolean completed = (Boolean) jt.get("completed");
                endpoint.completeTask(uuid, completed ? new Date() : null, user);
            }
        }
    }

    private void handleUpdate(JSONObject obj) {
        String kind = (String)obj.get("kind");
        if (kind.equals("gtask")) {
            for (Object t : (JSONArray)obj.get("content")) {
                JSONObject jt = (JSONObject)t;
                String id = (String)jt.get("id");

                Task task = (Task)endpoint.getItemsByGtaskId(new IdList(Collections.singletonList(id)), user).convertToItems().get(0);
                Gtask g = Util.toGtask(task, id);
                GtaskList gList = new GtaskList(Collections.singletonList(g), task.getParentUuid());
                if (jt.containsKey("parentId")) {
                    String parentUuid = (String) jt.get("parentId");
                    if (parentUuid != null) {
                        Task parent = (Task)endpoint.getItemsByGtaskId(new IdList(Collections.singletonList(parentUuid)), user).convertToItems().get(0);
                        parentUuid = parent.getUuid();
                    }
                    gList.setParentUuid(parentUuid);
                }
                if (jt.containsKey("completed")) {
                    Date date = new Date();
                    Date completed = null;
                    if((Boolean)jt.get("completed")) {
                        completed = date;
                    }
                    g.setCompleted(completed);
                    g.setUpdated(date);
                }
                endpoint.updateGtaskList(gList, user);
            }

        } else {
            throw new RuntimeException("Unsupported kind");
        }

    }

    private void handleCheck(JSONObject obj) {
        String kind = (String)obj.get("kind");
        if (kind.equals("task")) {
            for (Object t : (JSONArray)obj.get("content")) {
                JSONObject jt = (JSONObject)t;
                Task task = null;
                if (jt.containsKey("id")) {
                    String id = (String) jt.get("id");
                    task = (Task) endpoint.getItemsByGtaskId(new IdList(Collections.singletonList(id)), user).convertToItems().get(0);
                } else if (jt.containsKey("uuid")) {
                    String id = (String) jt.get("uuid");
                    task = (Task) endpoint.getItemsByUuid(new IdList(Collections.singletonList(id)), user).convertToItems().get(0);
                } else {
                    throw new RuntimeException("can't define content element");
                }

                if (jt.containsKey("completed")) {
                    assertEquals((Boolean)jt.get("completed"), task.isCompleted());
                }
                if (jt.containsKey("completedCount")) {
                    long c = (Long) jt.get("completedCount");
                    assertEquals(c, task.getSubtask().getCompletedCount());
                }
                if (jt.containsKey("totalCount")) {
                    long c = (Long) jt.get("totalCount");
                    assertEquals(c, task.getSubtask().getTotalCount());
                }
            }

        }

    }

}
