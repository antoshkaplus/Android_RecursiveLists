package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;


import java.security.InvalidParameterException;
import java.util.Date;
import java.util.logging.Logger;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * An endpoint class we are exposing
 */
@Api(
        name = "itemsApi",
        version = "v2",
        resource = "items",
        namespace = @ApiNamespace(
                ownerDomain = "backend.recursivelists.antoshkaplus.com",
                ownerName = "backend.recursivelists.antoshkaplus.com"
        ),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_HOME, Constants.ANDROID_CLIENT_ID_RELEASE,
                Constants.API_EXPLORER_CLIENT_ID, Constants.ANDROID_CLIENT_ID_WORK},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class ItemsEndpoint {

    static {
        ObjectifyService.register(Item.class);
        ObjectifyService.register(Task.class);
        ObjectifyService.register(BackendUser.class);
    }

    private static final Logger logger = Logger.getLogger(ItemsEndpoint.class.getName());

    @ApiMethod(name = "getChildrenItems", path = "get_children_items")
    public ItemList getChildrenItems(@Named("parentUuid")String uuid, User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("");
        }
        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).filter("parentUuid ==", uuid).list();
        return new ItemList(itemList);
    }

    @ApiMethod(name = "getItems", path = "get_items")
    public ItemList getItems(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("");
        }

        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).list();
        return new ItemList(itemList);
    }

    // should be created on creation of new user and taken by clients
    @ApiMethod(name = "getRootUuid", path = "get_root_uuid")
    public Uuid getRootUuid(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("");
        }

        BackendUser backendUser = retrieveBackendUser(user);
        return new Uuid(backendUser.getRootUuid());
    }

    @ApiMethod(name = "getGtaskLastUpdate", path = "get_gtask_last_update")
    public ResourceDate getGtaskLastUpdate(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        return new ResourceDate(backendUser.getGtaskLastUpdate());
    }

    @ApiMethod(name = "updateGtaskLastUpdate", path = "update_gtask_last_update")
    public void updateGtaskLastUpdate(final User user, final ResourceDate lastUpdate) {

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                if (backendUser.getGtaskLastUpdate().before(lastUpdate.value)) {
                    backendUser.setGtaskLastUpdate(lastUpdate.value);
                    ofy().save().entity(backendUser).now();
                }
            }
        });
    }

    @ApiMethod(name = "addGoogleTask", path = "add_google_task")
    public void addGtask(User user, GtaskList gtaskList) {


    }

    @ApiMethod(name = "addItemOnline", path = "add_item_online")
    public void addItemOnline(final Item item, final User user)
            throws OAuthRequestException, InvalidParameterException {
        try {
            ofy().transact(new VoidWork() {
                @Override
                public void vrun() {
                    BackendUser backendUser = retrieveBackendUser(user);
                    item.setOwner(backendUser);
                    item.setDbVersion(backendUser.increaseVersion());
                    ofy().save().entities(backendUser, item).now();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // have to update if parent is a task itself.
    @ApiMethod(name = "addTaskOnline", path = "add_task_online")
    public void addTaskOnline(final Task task, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                task.setOwner(backendUser);
                task.setDbVersion(backendUser.increaseVersion());
                if (!task.getParentUuid().equals(backendUser.getRootUuid())) {
                    // parent is real
                    Item i = ofy().load().type(Item.class).parent(backendUser).id(task.getParentUuid()).now();
                    if (i.isTask()) {
                        Task t = (Task)i;
                        t.getSubtask().incCount();
                        // will uncomplete next
                        t.getSubtask().incCompleted();
                        UncompleteTask ut = new UncompleteTask(backendUser.getVersion());
                        AncestorTraversal at = new AncestorTraversal(backendUser, ut);
                        at.traverse(task);
                        ofy().defer().save().entity(t);
                    }
                }
                ofy().save().entities(backendUser, task).now();
            }
        });
    }

    @ApiMethod(name = "removeTask", path = "remove_task")
    public void removeTask(final Task task, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                task.setOwner(backendUser);
                task.setDbVersion(backendUser.increaseVersion());
                task.setDisabled(true);
                if (!task.getParentUuid().equals(backendUser.getRootUuid())) {
                    // parent is real
                    Item i = ofy().load().type(Item.class).parent(backendUser).id(task.getParentUuid()).now();
                    if (i.isTask()) {
                        Task t = (Task)i;
                        t.getSubtask().decCount();
                        // will uncomplete next
                        if (!task.isCompleted()) {
                            t.getSubtask().decCompleted();
                            CompleteTask ut = new CompleteTask(new Date(), backendUser.getVersion());
                            AncestorTraversal at = new AncestorTraversal(backendUser, ut);
                            at.traverse(task);

                        } else {
                            t.getSubtask().decCompleted();
                        }
                        ofy().defer().save().entity(t);
                    }
                }
                ofy().save().entities(backendUser, task).now();
            }
        });
    }

    // you don't have to supply all the information about item
    // uuid and new parent uuid should be enough
    @ApiMethod(name = "moveItem", path = "move_item")
    public void moveItem(final Item item, final User user) {
        // this one is super easy
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Item oldItem = ofy().load().type(Item.class).parent(backendUser).id(item.getUuid()).now();
                oldItem.setParentUuid(item.getParentUuid());
                oldItem.setDbVersion(backendUser.increaseVersion());
                ofy().save().entities(backendUser, item).now();
            }
        });
    }

    // we have to take a look at ancestor and decrease tasks
    // it;s like removing and adding task again
    @ApiMethod(name = "moveTask", path = "move_task")
    public void moveTask(final Task task, final User user) {
        // so it's similar to calling moveItem
        // but we have to traverse ancestors first
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Task t = ofy().load().type(Task.class).parent(backendUser).id(task.getUuid()).now();
                removeTask(t, user);
                addTaskOnline(task, user);
            }
        });
    }



    @ApiMethod(name = "completeTask", path = "completeTask")
    public void completeTask(final Task task, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Task old = ofy().load().type(Task.class).parent(backendUser).id(task.getUuid()).now();
                // already set as completed
                if (old.getCompleteDate() != null) return;
                int dbVersion = backendUser.increaseVersion();
                task.setDbVersion(dbVersion);
                task.setOwner(backendUser);
                ofy().defer().save().entity(task);

                CompleteTask ct = new CompleteTask(task.getCompleteDate(), dbVersion);
                AncestorTraversal at = new AncestorTraversal(backendUser, ct);
                at.traverse(task);
            }
        });
    }


    public void updateTask() {
        // while updating one element check db version
        // DB version always has to increased, backend user resaved
        // how to force it?
    }


    private BackendUser retrieveBackendUser(User user) {
        BackendUser newUser = new BackendUser(user.getEmail());
        BackendUser res = ofy().load().entity(newUser).now();
        if (res == null) {
            ofy().save().entity(newUser).now();
            res = newUser;
        }
        return res;
    }

}