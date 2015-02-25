package com.antoshkaplus.recursivelists;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by antoshkaplus on 2/22/15.
 */
public class Item {

    public String title;
    public int order;
    public int parentId;
    public int id;

    public Item(int id, String title, int order, int parentId) {
        this.id = id;
        this.title = title;
        this.order = order;
        this.parentId = parentId;
    }

    JSONObject toJson() throws JSONException {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("order", order);
        jsonObject.put("parentId", parentId);
        jsonObject.put("key", id);
        return jsonObject;
    }

}
