package com.antoshkaplus.recursivelists.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

/**
 * Created by Anton.Logunov on 4/18/2015.
 */
@DatabaseTable(tableName = UserItem.TABLE_NAME)
public class UserItem {

    public static final String TABLE_NAME = "user_item";

    public static final String FIELD_NAME_USER = "userId";
    public static final String FIELD_NAME_ID = "id";


    @DatabaseField(columnName = FIELD_NAME_USER)
    public String userId;

    @DatabaseField(columnName = FIELD_NAME_ID, id = true)
    public UUID id;

}
