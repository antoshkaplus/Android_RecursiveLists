package com.antoshkaplus.recursivelists.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;


/**
 * Created by antoshkaplus on 3/9/15.
 */
@DatabaseTable(tableName = RemovedItem.TABLE_NAME)
public class RemovedItem {

    public static final String TABLE_NAME = "removed_item";

    public static final String FIELD_ITEM = "item_id";
    public static final String FIELD_DELETION_DATE = "deletion_date";

    @DatabaseField(columnName = FIELD_ITEM, foreign = true)
    public Item item;
    @DatabaseField(columnName = FIELD_DELETION_DATE)
    public Date deletionDate = new Date();

    public RemovedItem() {}

    public RemovedItem(Item item) {
        this.item = item;
    }

    public RemovedItem(Item item, Date deletionDate) {
        this.item = item;
        this.deletionDate = deletionDate;
    }

}
