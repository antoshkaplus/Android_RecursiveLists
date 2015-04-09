package com.antoshkaplus.recursivelists.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.logging.Logger;

import javax.inject.Named;
import javax.jdo.PersistenceManager;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "userItemsApi",
        version = "v1",
        resource = "userItems",
        namespace = @ApiNamespace(
                ownerDomain = "backend.recursivelists.antoshkaplus.com",
                ownerName = "backend.recursivelists.antoshkaplus.com",
                packagePath = ""
        )
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
        mgr.makePersistent(userItems);
    }


    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }
}