package com.antoshkaplus.recursivelists.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;
import java.util.Date;

/**
 * Created by antoshkaplus on 2/22/15.
 */

// we don't use here boolean deleted field, because
// we want to have deletionDate or something like this
// and we think that there will be small amount of deleted items
// that's why we don't want to have a separate column for that
// you delete only one item to get rid of the whole subtree
// that's how relational dbs work
@DatabaseTable(tableName = Item.TABLE_NAME)
public class Item {

    public static final String TABLE_NAME = "item";

    public static final String FIELD_NAME_TITLE = "title";
    public static final String FIELD_NAME_ORDER = "order";
    public static final String FIELD_NAME_PARENT_ID = "parent_id";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_STATE = "state";
    public static final String FIELD_NAME_CREATE_DATE = "create_date";
    public static final String FIELD_NAME_UPDATE_DATE = "update_date";

    @DatabaseField(columnName = FIELD_NAME_TITLE, canBeNull = false)
    public String title;
    @DatabaseField(columnName = FIELD_NAME_ORDER)
    public int order;

    // should not forget to assign
    @DatabaseField(columnName = FIELD_NAME_PARENT_ID, index = true, canBeNull = false)
    public UUID parentId;

    @DatabaseField(columnName = FIELD_NAME_ID, id = true, canBeNull = false)
    public UUID id;

    @DatabaseField(columnName = FIELD_NAME_STATE, canBeNull = false)
    public ItemState state = ItemState.Local;

    @DatabaseField(columnName = FIELD_NAME_CREATE_DATE, canBeNull = false)
    public Date createDate = new Date();

    @DatabaseField(columnName = FIELD_NAME_UPDATE_DATE, canBeNull = false)
    public Date updateDate = new Date();

    public Item(String title, int order, UUID parentId, ItemState state) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.order = order;
        this.parentId = parentId;
        this.state = state;
    }

    public Item(String title, int order, UUID parentId) {
        this(title, order, parentId, ItemState.Local);
    }

    // should be called by orm
    public Item() {
        this.id = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return title;
    }


    public ItemKind getItemKind() {
        return ItemKind.Item;
    }
}
