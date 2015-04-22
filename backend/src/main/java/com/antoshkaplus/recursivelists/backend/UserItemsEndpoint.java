package com.antoshkaplus.recursivelists.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_HOME, Constants.ANDROID_CLIENT_ID_RELEASE,
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
            items = createNewUserItems(user.getEmail());
            mgr.makePersistent(items);
        } finally {
            mgr.close();
        }
        return items;
    }

    @ApiMethod(name = "deleteUserItems")
    public void deleteUserItems(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("");
        }
        PersistenceManager mgr = getPersistenceManager();
        try {
            mgr.deletePersistent(mgr.getObjectById(UserItems.class, user.getEmail()));
        } catch (JDOObjectNotFoundException ex) {
            // everything is fine
        } finally {
            mgr.close();
        }
    }

    @ApiMethod(name = "updateUserItems")
    public void updateUserItems(UserItems userItems, User user) throws OAuthRequestException, InvalidParameterException {
        if (user == null) {
            throw new OAuthRequestException("");
        }

        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            tx.begin();
            UserItems curUI;
            try {
                curUI = mgr.getObjectById(UserItems.class, user.getEmail());
            } catch (JDOObjectNotFoundException ex) {
                // shouldn't be empty somehow
                curUI = null;
            }
            if (curUI != null && !curUI.getVersion().equals(userItems.getVersion())) {
                throw new InvalidParameterException("version update is invalid");
            }
            // need to be copied
            curUI.setRootId(userItems.getRootId());
            curUI.setVersion(curUI.getVersion() + 1);

            curUI.getItems().clear();
            curUI.getItems().addAll(userItems.getItems());
            //curUI.setItems(userItems.getItems());
            mgr.makePersistentAll(curUI.getItems());
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            throw ex;
        } finally {
            mgr.close();
        }
    }

    UserItems createNewUserItems(String id) {
        UserItems items = new UserItems();
        items.setUserId(id);
        items.setVersion(0);
        return items;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }


}