package com.antoshkaplus.recursivelists.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.antoshkaplus.recursivelists.model.Task;
import com.antoshkaplus.recursivelists.model.UserItem;
import com.antoshkaplus.recursivelists.model.UserRoot;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.UUID;

// functionality of this class is already quite big :
// we should keep track of data base version and make
// correct conversions.
// lets not fill it with queries of clients
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "recursive_lists";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 4;

    // the DAO object we use to access the SimpleData table
    private Dao<Item, Integer> itemsDao;
    private Dao<Task, Integer> tasksDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Item.class);
            TableUtils.createTable(connectionSource, Task.class);
            TableUtils.createTable(connectionSource, RemovedItem.class);
            TableUtils.createTable(connectionSource, UserRoot.class);
            TableUtils.createTable(connectionSource, UserItem.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            try {
                TableUtils.createTable(connectionSource, Task.class);
            } catch (java.sql.SQLException e) {
                Log.e(this.getClass().getName(), "Unable to do database migration from | to |", e);
                throw new RuntimeException(e);
            }
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE `item` ADD COLUMN create_date DATE NOT NULL");
            db.execSQL("ALTER TABLE `item` ADD COLUMN update_date DATE NOT NULL");
        }
    }
}