package com.antoshkaplus.recursivelists.model;

import org.json.JSONObject;
import org.json.JSONException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

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
    @DatabaseField(columnName = FIELD_NAME_PARENT_ID, index = true)
    public UUID parentId;

    @DatabaseField(columnName = FIELD_NAME_ID, id = true)
    public UUID id;


    public Item(String title, int order, UUID parentId) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.order = order;
        this.parentId = parentId;
    }

    // should be called by orm
    public Item() {}

    @Override
    public String toString() {
        return title;
    }

}
