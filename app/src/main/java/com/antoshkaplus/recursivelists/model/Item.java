package com.antoshkaplus.recursivelists.model;

import org.json.JSONObject;
import org.json.JSONException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by antoshkaplus on 2/22/15.
 */

@DatabaseTable(tableName = Item.TABLE_NAME)
public class Item {

    public static final String TABLE_NAME = "item";

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

    public Item(JSONObject json) throws JSONException {
        title = json.getString(FIELD_NAME_TITLE);
        order = json.getInt(FIELD_NAME_ORDER);
        parentId = json.getInt(FIELD_NAME_PARENT_ID);
        id = json.getInt(FIELD_NAME_ID);
    }

    // should be called by orm
    public Item() {}

    @Override
    public String toString() {
        return title;
    }

    JSONObject toJson() throws JSONException {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put(FIELD_NAME_TITLE, title);
        jsonObject.put(FIELD_NAME_ORDER, order);
        jsonObject.put(FIELD_NAME_PARENT_ID, parentId);
        jsonObject.put(FIELD_NAME_ID, id);
        return jsonObject;
    }

}
