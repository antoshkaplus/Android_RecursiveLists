package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.GtaskTrack;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
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
        ObjectifyService.register(GtaskTrack.class);
    }

    private static final Logger logger = Logger.getLogger(ItemsEndpoint.class.getName());

    @ApiMethod(name = "getChildrenItems", path = "get_children_items")
    public ItemList getChildrenItems(@Named("parentUuid")String uuid, User user) throws OAuthRequestException {

        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).filter("parentUuid ==", uuid).list();
        return new ItemList(itemList);
    }

    @ApiMethod(name = "getItems", path = "get_items")
    public ItemList getItems(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).list();
        return new ItemList(itemList);
    }

    @ApiMethod(name = "getRootUuid", path = "get_root_uuid")
    public Uuid getRootUuid(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        return new Uuid(backendUser.getRootUuid());
    }

    // TODO may not be needed
    @ApiMethod(name = "getGtaskLastUpdate", path = "get_gtask_last_update")
    public ResourceDate getGtaskLastUpdate(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        return new ResourceDate(backendUser.getGtaskLastUpdate());
    }

    // TODO may not be needed
    @ApiMethod(name = "updateGtaskLastUpdate", path = "update_gtask_last_update")
    public void updateGtaskLastUpdate(final ResourceDate lastUpdate, final User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                if (backendUser.getGtaskLastUpdate().before(lastUpdate.value)) {
                    backendUser.setGtaskLastUpdate(lastUpdate.value);
                    ofy().save().entity(backendUser).now();
                }
            }
        });
    }

    // checks if gtask moved to the App
    // if not id is set to null
    @ApiMethod(name = "checkGtaskIdPresent", path = "check_gtask_id_present")
    public IdList checkGtaskIdPresent(final IdList idList, final User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        Map<String, GtaskTrack> tracks = ofy().load().type(GtaskTrack.class).parent(backendUser).ids(idList.getIds());
        List<String> ids = idList.getIds();
        for (int i = 0; i < ids.size(); ++i) {
            if (tracks.get(ids.get(i)) == null) {
                ids.set(i, null);
            }
        }
        return idList;
    }

    @ApiMethod(name = "addGtaskList", path = "add_gtask_list")
    public void addGtaskList(final GtaskList gtaskList, User user) {

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                try {
                    BackendUser backendUser = retrieveBackendUser(user);
                    int V = backendUser.increaseVersion();

                    List<String> ids = new ArrayList<>(gtaskList.getGtasks().size());
                    gtaskList.getGtasks().forEach((gtask) -> {
                        ids.add(gtask.getId());
                    });
                    Map<String, GtaskTrack> map = ofy().load().type(GtaskTrack.class).parent(backendUser).ids(ids);

                    for (Gtask gtask : gtaskList.getGtasks()) {
                        GtaskTrack track = map.get(gtask.getId());
                        if (track == null) {
                            // have to add new item
                            track = new GtaskTrack();
                            track.googleId = gtask.getId();
                            track.owner = backendUser.getKey();

                            Task newTask = Util.toTask(gtask, backendUser, gtaskList.getParentUuid());
                            addNewTask(newTask, backendUser);
                            track.task = Ref.create(newTask);
                            ofy().save().entity(track).now();
                        } else {

                            Task serverTask = track.task.get();
                            if (gtask.getUpdated().after(serverTask.getUpdateDate())) {
                                // this is actually move operation
                                if (!serverTask.getParentUuid().equals(gtaskList.getParentUuid())) {
                                    // we have to update subtasks

                                }
                                serverTask.setParentUuid(gtaskList.getParentUuid());
                                serverTask.setUpdateDate(gtask.getUpdated());
                                serverTask.setCompleteDate(gtask.getCompleted());
                                serverTask.setTitle(gtask.getTitle());
                                serverTask.setDbVersion(V);

                                // to check for cycles
                                //moveTask(serverTask, user);
                                ofy().save().entity(serverTask);
                            }

                        }
                    }
                    ofy().save().entity(backendUser);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        });
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
                addNewTask(task, backendUser);
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
                        ofy().save().entity(t).now();
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
                ofy().save().entity(task).now();

                CompleteTask ct = new CompleteTask(task.getCompleteDate(), dbVersion);
                AncestorTraversal at = new AncestorTraversal(backendUser, ct);
                at.traverse(task);
            }
        });
    }

    private void removeTask(String taskId, BackendUser backendUser) {
        Task task = ofy().load().type(Task.class).parent(backendUser).id(taskId).now();
        task.setDbVersion(backendUser.getVersion());
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
                ofy().save().entity(t).now();
            }
        }
        ofy().save().entities(backendUser, task).now();

    }


    // consider that task is already created
    private void completeTask(Task task, BackendUser backendUser) {


    }

    private void updateTask(Task task, BackendUser backendUser) {


    }


    private void addNewTask(Task task, BackendUser backendUser) {
        task.setOwner(backendUser);
        if (!task.isValid()) throw new RuntimeException("addNewTask: task is invalid");

        task.setDbVersion(backendUser.getVersion());
        if (!task.getParentUuid().equals(backendUser.getRootUuid())) {

            Item i = ofy().load().type(Item.class).parent(backendUser).id(task.getParentUuid()).now();
            if (i.isTask()) {
                Task t = (Task)i;
                t.getSubtask().incCount();
                t.getSubtask().incCompleted();
                if (!task.isCompleted()) {
                    UncompleteTask ut = new UncompleteTask(backendUser.getVersion());
                    AncestorTraversal at = new AncestorTraversal(backendUser, ut);
                    at.traverse(task);
                }
                ofy().save().entity(t).now();
            }
        }
        ofy().save().entity(task).now();
    }


    private BackendUser retrieveBackendUser(User user) {
        if (user == null) {
            throw new RuntimeException("user is null");
        }
        BackendUser newUser = new BackendUser(user.getEmail());
        BackendUser res = ofy().load().entity(newUser).now();
        if (res == null) {
            ofy().save().entity(newUser).now();
            res = newUser;
        }
        return res;
    }




}