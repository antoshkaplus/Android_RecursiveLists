package com.antoshkaplus.recursivelists.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "userItemsApi",
        version = "v1",
        resource = "userItems",
        namespace = @ApiNamespace(
                ownerDomain = "backend.recursivelists.antoshkaplus.com",
                ownerName = "backend.recursivelists.antoshkaplus.com"
        ),
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_HOME,
                Constants.API_EXPLORER_CLIENT_ID, Constants.ANDROID_CLIENT_ID_WORK},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class UserItemsEndpoint {

    private static final Logger logger = Logger.getLogger(UserItemsEndpoint.class.getName());

    @ApiMethod(name = "getUserItems")
    public UserItems getUserItems(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("");
        }
        PersistenceManager mgr = getPersistenceManager();
        UserItems items = null;
        try {
            items = mgr.getObjectById(UserItems.class, user.getEmail());
        } catch (JDOObjectNotFoundException ex){
            items = new UserItems();
            items.setUserId(user.getEmail());
            items.setItems(new ArrayList<Item>());
            items.setRemovedItems(new ArrayList<RemovedItem>());
            mgr.makePersistent(items);
        } finally {
            mgr.close();
        }
        return items;
    }

    @ApiMethod(name = "insertUserItems")
    public void insertUserItems(UserItems userItems, User user) throws OAuthRequestException{
        if (user == null) {
            throw new OAuthRequestException("");
        }
        PersistenceManager mgr = getPersistenceManager();
        userItems.setUserId(user.getEmail());
        Transaction tx = mgr.currentTransaction();
        try {
            tx.begin();
            mgr.makePersistent(userItems);
            mgr.makePersistentAll(userItems.getItems());
            mgr.makePersistentAll(userItems.getRemovedItems());
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
        }
        mgr.close();
    }


    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }
}