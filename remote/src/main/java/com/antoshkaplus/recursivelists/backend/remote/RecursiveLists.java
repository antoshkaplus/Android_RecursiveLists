package com.antoshkaplus.recursivelists.backend.remote;


import com.antoshkaplus.recursivelists.backend.Traversal;
import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.ItemKind;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;
import com.googlecode.objectify.ObjectifyService;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class RecursiveLists {

    static {
        ObjectifyService.register(Item.class);
        ObjectifyService.register(Task.class);
        ObjectifyService.register(BackendUser.class);
    }

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
            ObjectifyService.begin();

            while (executeCommand());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        installer.uninstall();
    }

    private RemoteApiOptions init() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Run dev? (Y/n): ");
        String s = scanner.nextLine().toLowerCase();
        String server = "localhost";
        if (s.equals("n") || s.equals("no")) {
            System.out.println("Run Production");
            return new RemoteApiOptions()
                    .server("antoshkaplus-recursivelists.appspot.com", 443)
                    .useApplicationDefaultCredential();
        } else {
            System.out.println("Run Development");
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
            System.out.println("correct-top-level-tasks");
            System.out.println("correct-disabled");
            System.out.println("save-tasks-as-tasks");
        }
        else if (command.equals("correct-top-level-tasks")) {
            forEachUser(this::correctDisabled);
        }
        else if (command.equals("correct-disabled")) {
            forEachUser(this::correctTopLevelTasks);
        }
        else if (command.equals("save-tasks-as-tasks")) {
            forEachUser(this::saveTasksAsTasks);
        }
        else if (command.equals("resave-items")) {
            forEachUser(this::resaveItems);
        }
        else {
            System.out.println("");
        }
        return true;
    }

    private List<BackendUser> getBackendUserList() {
        return ofy().load().type(BackendUser.class).list();
    }

    interface UserHandler {
        void Handle(BackendUser user);
    }

    private void forEachUser(UserHandler handler) {
        List<BackendUser> users = getBackendUserList();
        users.forEach((user) -> {
            ofy().transact(() -> {
                user.increaseVersion();
                handler.Handle(user);
                ofy().defer().save().entities(user);
                return null;
            });
        });
    }

    void correctTopLevelTasks(BackendUser user) {

        String uuid = user.getRootUuid();
        Item rootItem = new Item();
        rootItem.setUuid(uuid);

        new Traversal<ItemKind>(user, rootItem, null).traverse((item, parentKind) -> {
            return new Traversal.Pair<ItemKind>(!item.isDisabled(), item.getKind());
        });
    }

    void correctDisabled(BackendUser user) {
        // Info is boolean isDisabled

        String uuid = user.getRootUuid();
        Item rootItem = new Item();
        rootItem.setUuid(uuid);

        final List<Item> changedItems = new ArrayList<>();

        new Traversal<Boolean>(user, rootItem, false).traverse((item, parentDisabled) -> {
            if (parentDisabled && !item.isDisabled()) {
                item.setDisabled(true);
                item.setDbVersion(user.getVersion());
                changedItems.add(item);
            }
            return new Traversal.Pair<Boolean>(true, item.isDisabled());
        });

        ofy().defer().save().entities(changedItems);
    }

    void saveTasksAsTasks(BackendUser user) {
        String uuid = user.getRootUuid();
        Item rootItem = new Item();
        rootItem.setUuid(uuid);

        final List<Task> tasks = new ArrayList<>();

        new Traversal<Void>(user, rootItem, null).traverse((item, parentDisabled) -> {
            if (item.isTask()) {
                tasks.add((Task)item);
            }
            return new Traversal.Pair<Void>(true, null);
        });
        System.out.println("Save tasks: " + tasks.size());
        ofy().defer().save().entities(tasks);
    }

    void resaveItems(BackendUser user) {
        String uuid = user.getRootUuid();
        Item rootItem = new Item();
        rootItem.setUuid(uuid);

        final List<Item> items = new ArrayList<>();

        new Traversal<Void>(user, rootItem, null).traverse((item, parentDisabled) -> {
            items.add(item);
            return new Traversal.Pair<Void>(true, null);
        });
        System.out.println("Resave items: " + items.size());
        ofy().defer().save().entities(items);
    }
}
