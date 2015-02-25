package com.antoshkaplus.recursivelists;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.ArrayList;
import java.util.List;

// functionality of this class is already quite big :
// we should keep track of data base version and make
// correct conversions.
// lets not fill it with queries of clients
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "recursive_lists";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 2;

    // the DAO object we use to access the SimpleData table
    private Dao<Item, Integer> itemsDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Item.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public Dao<Item, Integer> getItemDao() {
        if (null == itemsDao) {
            try {
                itemsDao = getDao(Item.class);
            }catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return itemsDao;
    }


}