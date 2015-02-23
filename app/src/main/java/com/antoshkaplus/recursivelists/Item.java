package com.antoshkaplus.recursivelists;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by antoshkaplus on 2/22/15.
 */
public class Item {

    String title;
    int order;
    int parentKey;
    int key;

    JSONObject toJson() throws JSONException {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put("title", title);
        jsonObject.put("order", order);
        jsonObject.put("parentKey", parentKey);
        jsonObject.put("key", key);
        return jsonObject;
    }

}
