package com.antoshkaplus.recursivelists;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Created by antoshkaplus on 2/22/15.
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Item {

    public static final String FIELD_NAME_TITLE = "title";
    public static final String FIELD_NAME_ORDER = "order";
    public static final String FIELD_NAME_PARENT_ID = "parent_id";
    public static final String FIELD_NAME_ID = "id";

    @DatabaseField(columnName = FIELD_NAME_TITLE)
    public String title;
    @DatabaseField(columnName = FIELD_NAME_ORDER)
    public int order;

    // should not forget to assign
    @DatabaseField(columnName = FIELD_NAME_PARENT_ID)
    public int parentId;

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    public int id;


    public Item(String title, int order, int parentId) {
        this.title = title;
        this.order = order;
        this.parentId = parentId;
    }

    public Item() {}

    @Override
    public String toString() {
        return title;
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
