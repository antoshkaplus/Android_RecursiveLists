package com.antoshkaplus.recursivelists.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

/**
 * Created by Anton.Logunov on 4/19/2015.
 */
@DatabaseTable(tableName = UserRoot.TABLE_NAME)
public class UserRoot {

    public static final String TABLE_NAME = "user_root";

    public static final String FIELD_NAME_USER = "user_id";
    public static final String FIELD_NAME_ROOT_ID = "root_id";
    public static final String FIELD_NAME_LAST_SYNC_VERSION = "last_sync_version";

    @DatabaseField(columnName = FIELD_NAME_USER, id = true)
    public String userId;

    @DatabaseField(columnName = FIELD_NAME_ROOT_ID, unique = true)
    public UUID rootId;

    @DatabaseField(columnName = FIELD_NAME_LAST_SYNC_VERSION, defaultValue = "0")
    public int lastSyncVersion;

    public UserRoot() {}

    public UserRoot(String userId, UUID rootId) {
        this.userId = userId;
        this.rootId = rootId;
    }
}
