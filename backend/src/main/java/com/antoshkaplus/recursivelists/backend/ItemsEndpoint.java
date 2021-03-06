package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.bee.ValContainer;
import com.antoshkaplus.bee.backend.ResourceDate;
import com.antoshkaplus.bee.backend.ResourceInteger;
import com.antoshkaplus.recursivelists.backend.bean.Gtask;
import com.antoshkaplus.recursivelists.backend.bean.GtaskList;
import com.antoshkaplus.recursivelists.backend.bean.IdList;
import com.antoshkaplus.recursivelists.backend.bean.TaskList;
import com.antoshkaplus.recursivelists.backend.bean.Uuid;
import com.antoshkaplus.recursivelists.backend.bean.VariantItem;
import com.antoshkaplus.recursivelists.backend.bean.VariantItemList;
import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.GtaskTrack;
import com.antoshkaplus.recursivelists.backend.model.Task;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;


import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.List;

import static ch.lambdaj.Lambda.forEach;
import static ch.lambdaj.Lambda.of;
import static ch.lambdaj.Lambda.on;
import static com.googlecode.objectify.ObjectifyService.ofy;
import static ch.lambdaj.Lambda.extract;


/**
 * An endpoint class we are exposing
 */
@Api(
        name = "itemsApi",
        version = "v5",
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
    public VariantItemList getChildrenItems(@Named("parentUuid")String uuid, User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).filter("parentUuid ==", uuid).list();
        return VariantItemList.createFromItems(itemList);
    }

    @ApiMethod(name = "getItems", path = "get_items")
    public VariantItemList getItems(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).filter("disabled", false).list();
        return VariantItemList.createFromItems(itemList);
    }

    @ApiMethod(name = "getItemList_G_Version", path = "get_item_list_g_version")
    public VariantItemList getItemList_G_Version(@Named("version")Integer version, User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        List<Item> itemList = ofy().load().type(Item.class).ancestor(backendUser).filter("version >", version).list();
        return VariantItemList.createFromItems(itemList);
    }

    @ApiMethod(name = "getCurrentTaskList", path = "get_current_task_list")
    public TaskList getCurrentTaskList(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        List<Task> taskList = ofy().load().type(Task.class).ancestor(backendUser).filter("current", true).filter("disabled", false).list();
        return new TaskList(taskList);
    }

    @ApiMethod(name = "getAllTaskList", path = "get_all_task_list")
    public TaskList getAllTaskList(User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        List<Task> taskList = ofy().load().type(Task.class).ancestor(backendUser).filter("disabled", false).list();
        return new TaskList(taskList);
    }

    @ApiMethod(name = "getRootUuid", path = "get_root_uuid")
    public Uuid getRootUuid(User user) {

        BackendUser backendUser = retrieveBackendUser(user);
        return new Uuid(backendUser.getRootUuid());
    }

    @ApiMethod(name = "getDbVersion", path = "get_db_version")
    public ResourceInteger getDbVersion(User user) {
        return new ResourceInteger(retrieveBackendUser(user).getVersion());
    }

    @ApiMethod(name = "getItem", path = "get_item")
    public VariantItem getItem(@Named("uuid")String uuid, User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        Item item = ofy().load().type(Item.class).parent(backendUser).id(uuid).now();
        return VariantItem.create(item);
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

        final BackendUser backendUser = retrieveBackendUser(user);
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

    // for newly added gtasks we could return back their uuid-s
    @ApiMethod(name = "addGtaskList", path = "add_gtask_list")
    public void updateGtaskList(final GtaskList gtaskList, final User user) {

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                try {
                    BackendUser backendUser = retrieveBackendUser(user);
                    int V = backendUser.increaseVersion();

                    List<String> ids = new ArrayList<>(
                            extract(gtaskList.getGtasks(), on(Gtask.class).getId()));

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
                            // parent change is mandatory despite of update date
                            updateTaskParent(serverTask, gtaskList.getParentUuid(), backendUser);
                            if (gtask.getUpdated().after(serverTask.getUpdateDate())) {
                                serverTask.setUpdateDate(gtask.getUpdated());
                                updateTaskComplete(serverTask, gtask.getCompleted(), backendUser);
                                serverTask.setTitle(gtask.getTitle());
                                serverTask.setDbVersion(V);

                                ofy().save().entity(serverTask).now();
                            }
                        }
                    }
                    ofy().save().entity(backendUser).now();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }
            }
        });
    }

    @ApiMethod(name = "getItemsById", path = "get_items_by_uuid")
    public VariantItemList getItemsByUuid(IdList idList, final User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        Map<String, Item> itemMap = ofy().load().type(Item.class).parent(backendUser).ids(idList.getIds());
        VariantItemList res = new VariantItemList();
        for (String id : idList.getIds()) {
            res.add(itemMap.get(id));
        }
        return res;
    }

    @ApiMethod
    public VariantItem getItemByUuid(@Named("uuid") String uuid, final User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        return VariantItem.create(ofy().load().type(Item.class).parent(backendUser).id(uuid).now());
    }

//    @ApiMethod(name = "getItemsGreaterVersion", path = "get_items_greater_version")
//    public ItemList getItemsGreaterVersion(Integer  final User user) {
//
//    }


    @ApiMethod(name = "getItemsByGtaskId", path = "get_items_by_gtask_id")
    public VariantItemList getItemsByGtaskId(IdList idList, final User user) {
        BackendUser backendUser = retrieveBackendUser(user);
        Map<String, GtaskTrack> itemMap = ofy().load().type(GtaskTrack.class).parent(backendUser).ids(idList.getIds());
        VariantItemList res = new VariantItemList();
        for (String id : idList.getIds()) {
            res.add(itemMap.get(id).task.get());
        }
        return res;
    }

    @ApiMethod(name = "addItem", path = "add_item")
    public VariantItem addVariantItem(final VariantItem item, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                backendUser.increaseVersion();
                addVariantItemImpl(item, backendUser);
                ofy().save().entities(backendUser);
            }
        });
        return item;
    }

    private void addVariantItemImpl(final VariantItem item, final BackendUser backendUser) {
        if (item.getItem() != null) {
            addNewItem(item.getItem(), backendUser);
        } else if (item.getTask() != null) {
            addNewTask(item.getTask(), backendUser);
        } else {
            throw new RuntimeException("Empty VariantItem");
        }
    }

    @ApiMethod(name = "removeItem", path = "remove_item", httpMethod = "POST")
    public void removeVariantItem(final VariantItem item, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                backendUser.increaseVersion();
                removeVariantItemImpl(item, backendUser);
                ofy().save().entities(backendUser);
            }
        });
    }

    private void removeVariantItemImpl(final VariantItem item, final BackendUser backendUser) {
        if (item.getItem() != null) {
            removeItem(item.getItem().getUuid(), backendUser);
        } else if (item.getTask() != null) {
            removeTask(item.getTask().getUuid(), backendUser);
        } else {
            throw new RuntimeException("Empty VariantItem");
        }
    }

    @ApiMethod(name = "addItemList", path = "add_item_list")
    public void addVariantItemList(VariantItemList itemList, final User user) {
        for (VariantItem i : itemList.getVariantItems()) {
            addVariantItem(i, user);
        }
    }

    @ApiMethod(name = "removeItemList", path = "remove_item_list", httpMethod = "POST")
    public void removeVariantItemList(VariantItemList itemList, final User user) {
        for (VariantItem i : itemList.getVariantItems()) {
            removeVariantItem(i, user);
        }
    }

    private void addNewItem(final Item item, final BackendUser backendUser) {
        item.setOwner(backendUser);
        if (item.getParentUuid() == null) {
            item.setParentUuid(backendUser.getRootUuid());
        }
        item.setDbVersion(backendUser.increaseVersion());

        if (!item.isValid()) {
            throw new RuntimeException("item is invalid: " + item.getValidityReport());
        }
        ofy().save().entities(item).now();
    }

    // you don't have to supply all the information about item
    // uuid and new parent uuid should be enough
    @ApiMethod(name = "moveItem", path = "move_item")
    public void moveItem(@Named("uuid") final String uuid, @Named("parentUuid") final String parentUuid, final User user) {
        // this one is super easy
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Item item = ofy().load().type(Item.class).parent(backendUser).id(uuid).now();
                item.setParentUuid(parentUuid == null ? backendUser.getRootUuid() : parentUuid);
                item.setDbVersion(backendUser.increaseVersion());
                ofy().save().entities(backendUser, item).now();
            }
        });
    }

    // we have to take a look at ancestor and decrease tasks
    // it;s like removing and adding task again
    @ApiMethod(name = "moveTask", path = "move_task")
    public void moveTask(@Named("uuid") final String uuid, @Named("parentUuid") final String parentUuid, final User user) {
        // so it's similar to calling moveItem
        // but we have to traverse ancestors first
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                backendUser.increaseVersion();
                Task t = ofy().load().type(Task.class).parent(backendUser).id(uuid).now();
                moveTask(t, parentUuid, backendUser);

                ofy().save().entity(backendUser).now();
            }
        });
    }


    @ApiMethod(name = "completeTask", path = "completeTask")
    public Task completeTask(@Named("uuid") final String uuid, @Named("completeDate")final Date completeDate, final User user) {
        ValContainer<Task> valTask = new ValContainer<>();
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Task task = ofy().load().type(Task.class).parent(backendUser).id(uuid).now();
                updateTaskComplete(task, completeDate, backendUser);
                ofy().save().entity(task).now();
                valTask.setVal(task);
            }
        });
        return valTask.getVal();
    }

    @ApiMethod(name = "setCurrentTask", path = "setCurrentTask")
    public Task setCurrentTask(@Named("uuid") final String uuid, @Named("current") final boolean current, final User user) {
        ValContainer<Task> valTask = new ValContainer<>();
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Task task = ofy().load().type(Task.class).parent(backendUser).id(uuid).now();
                task.setDbVersion(backendUser.increaseVersion());
                task.setCurrent(current);
                ofy().save().entity(task).now();
                valTask.setVal(task);
            }
        });
        return valTask.getVal();
    }

    @ApiMethod(name = "setPriority", path = "setPriority")
    public Task setPriority(@Named("uuid") final String uuid, @Named("priority") final int priority, final User user) {
        ValContainer<Task> valTask = new ValContainer<>();
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                Task task = ofy().load().type(Task.class).parent(backendUser).id(uuid).now();
                task.setDbVersion(backendUser.increaseVersion());
                task.setPriority(priority);
                ofy().save().entity(task).now();
                valTask.setVal(task);
            }
        });
        return valTask.getVal();
    }

    private void removeTask(String taskId, BackendUser backendUser) {
        Task task = ofy().load().type(Task.class).parent(backendUser).id(taskId).now();
        task.setDbVersion(backendUser.getVersion());
        task.setDisabled(true);
        detachTask(task, backendUser);
        ofy().save().entities(backendUser, task).now();
    }

    private void removeItem(String itemId, BackendUser backendUser) {
        Item item = ofy().load().type(Task.class).parent(backendUser).id(itemId).now();
        item.setDbVersion(backendUser.getVersion());
        item.setDisabled(true);
        ofy().save().entities(backendUser, item).now();
    }


    // consider that task is already created
    private void updateTaskComplete(Task task, Date completeDate, BackendUser backendUser) {
        new SubtaskManagement(backendUser).updateCompleteDate(task, completeDate);
    }

    private void updateTaskParent(Task task, String parentUuid, BackendUser user) {
        if (parentUuid == null) {
            parentUuid = user.getRootUuid();
        }
        if (task.getParentUuid().equals(parentUuid)) {
            return;
        }
        moveTask(task, parentUuid, user);
    }

    // 1) subtask counts
    // 2) discover cycle
    private void moveTask(Task task, Item newParent, BackendUser backendUser) {
        if (newParent != null) {
            if (task.getUuid().equals(newParent.getUuid())) {
                throw new RuntimeException("Can't be a parent of yourself.");
            }
            if (hasAncestor(newParent, task, backendUser)) {
                throw new RuntimeException("Move task action creates a cycle.");
            }
        }
        detachTask(task, backendUser);
        attachTask(task, newParent, backendUser);
    }

    private void moveTask(Task task, String parentId, BackendUser backendUser) {
        Item parent = null;
        if (parentId != null && !parentId.equals(backendUser.getRootUuid())) {
            parent = ofy().load().type(Item.class).parent(backendUser).id(parentId).now();
        }
        moveTask(task, parent, backendUser);
    }

    // used to detect cycle
    private boolean hasAncestor(Item item, final Item ancestor, BackendUser user) {
        final ValContainer<Boolean> hasAncestor = new ValContainer<>(false);
        AncestorTraversal at = new AncestorTraversal(user, item);
        at.traverse(new AncestorTraversal.Handler() {
            @Override
            public boolean handle(Item a) {
                if (ancestor.getUuid().equals(a.getUuid())) {
                    hasAncestor.setVal(true);
                    return false;
                }
                return true;
            }
        });
        return hasAncestor.getVal();
    }


    private void detachTask(Task task, BackendUser backendUser) {
        new SubtaskManagement(backendUser).detach(task);
    }

    private void attachTask(Task task, Item newParent, BackendUser backendUser) {
        if (newParent == null) {
            task.setParentUuid(backendUser.getRootUuid());
        } else {
            task.setParentUuid(newParent.getUuid());
            new SubtaskManagement(backendUser).attach(task);
        }
        ofy().save().entity(task).now();
    }

    private void attachTask(Task task, BackendUser user) {
        if (task.getParentUuid().equals(user.getRootUuid())) {
            return;
        }
        Item parent = ofy().load().type(Item.class).parent(user).id(task.getParentUuid()).now();
        attachTask(task, parent, user);
    }

    private void addNewTask(Task task, BackendUser backendUser) {
        task.setOwner(backendUser);
        if (task.getParentUuid() == null) {
            task.setParentUuid(backendUser.getRootUuid());
        }
        if (!task.isValid()) {
            throw new RuntimeException("task is invalid: " + task.getValidityReport());
        }

        task.setDbVersion(backendUser.getVersion());
        attachTask(task, backendUser);

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