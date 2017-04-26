package com.antoshkaplus.recursivelists.backend.remote;


import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.ItemKind;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

import com.google.appengine.api.datastore.DatastoreService;


import static com.googlecode.objectify.ObjectifyService.ofy;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class RecursiveLists {


    static {
        ObjectifyService.register(Item.class);
        ObjectifyService.register(Task.class);
        ObjectifyService.register(BackendUser.class);
    }

    BackendUser user;
    RemoteApiInstaller installer;


    public static void main(String[] args) {
        RecursiveLists words = new RecursiveLists();
        words.run();
    }


    private void run() {
        RemoteApiOptions options = new RemoteApiOptions()
                .server("localhost", 8080).useDevelopmentServerCredential();

        installer = new RemoteApiInstaller();
        try {
            installer.install(options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ObjectifyService.begin();

        installer.uninstall();
    }

}
