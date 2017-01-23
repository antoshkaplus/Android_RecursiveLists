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

    @ApiMethod(name = "addTaskOnline", path = "add_task_online")
    public void addTaskOnline(final Task task, final User user) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                BackendUser backendUser = retrieveBackendUser(user);
                task.setOwner(backendUser);
                task.setDbVersion(backendUser.increaseVersion());
                ofy().save().entities(backendUser, task).now();
            }
        });
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