package com.antoshkaplus.recursivelists.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;


/**
 * Created by antoshkaplus on 3/9/15.
 */
@DatabaseTable(tableName = RemovedItem.TABLE_NAME)
public class RemovedItem {

    public static final String TABLE_NAME = "removed_item";

    public static final String FIELD_ITEM_ID = "item_id";
    public static final String FIELD_ITEM_KIND = "item_kind";
    public static final String FIELD_DELETION_DATE = "deletion_date";

    // we can't use object reference here as we have multiple tables with keys
    @DatabaseField(columnName = FIELD_ITEM_ID, canBeNull = false)
    public UUID itemId;
    @DatabaseField(columnName = FIELD_ITEM_KIND, canBeNull = false)
    public ItemKind itemKind;
    @DatabaseField(columnName = FIELD_DELETION_DATE, canBeNull = false)
    public Date deletionDate = new Date();


    public RemovedItem() {}

    public RemovedItem(Item item) {
        this.itemId = item.id;
        this.itemKind = item.getItemKind();
    }

    public RemovedItem(Item item, Date deletionDate) {
        this(item);
        this.deletionDate = deletionDate;
    }

}
