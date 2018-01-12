package com.antoshkaplus.recursivelists.backend.remote;


import com.antoshkaplus.recursivelists.backend.Traversal;
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
import java.util.Stack;


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
        RemoteApiOptions options = init();

        installer = new RemoteApiInstaller();
        try {
            installer.install(options);

            while (executeCommand());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ObjectifyService.begin();

        installer.uninstall();
    }

    private RemoteApiOptions init() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Run dev? (Y/n): ");
        String s = scanner.nextLine().toLowerCase();
        String server = "localhost";
        if (s.equals("n") || s.equals("no")) {
            return new RemoteApiOptions()
                    .server("antoshkaplus-recursivelists.appspot.com", 443)
                    .useApplicationDefaultCredential();
        } else {
            return new RemoteApiOptions()
                    .server("localhost", 8080)
                    .useDevelopmentServerCredential();
        }
    }

    boolean executeCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("type command:");
        String command = scanner.nextLine();
        if (command.equals("exit")) {
            return false;
        }
        else if (command.equals("list")) {
            // try to use abbreviations for commands
        }
        else if (command.equals("correct-top-level-tasks")) {
            List<BackendUser> users = getBackendUserList();

        }
        else if (command.equals("correct-disabled")) {
            List<BackendUser> users = getBackendUserList();

        }
        else {
            System.out.println("");
        }
        return true;
    }

    private List<BackendUser> getBackendUserList() {
        return ofy().load().type(BackendUser.class).list();
    }

    void correctTopLevelTasks(BackendUser user) {
        class P {
            Item item;
            ItemKind parentKind;
            boolean parentDisabled;

            P(Item item, ItemKind parentKind, boolean parentDisabled) {
                this.item = item;
                this.parentKind = parentKind;
                this.parentDisabled = parentDisabled;
            }
        }

        String uuid = user.getRootUuid();
        Item item = new Item();
        item.setUuid(uuid);

        P p = new P(item, ItemKind.Item, false);

        Stack<P> ps = new Stack<>();
        ps.push(p);
        while (!ps.empty()) {
            p = ps.pop();
            if (p.item.isTask()) {

            } else {

            }
                // i need a way to save after i'm done
        }

    }

    void correctDisabled(BackendUser user) {
        // Info is boolean isDisabled

        Traversal<>
    }

    List<Item> getChildren(BackendUser user, String parentUuid) {
        return ofy().load().type(Item.class).ancestor(user).filter("parentUuid ==", parentUuid).list();
    }
}
