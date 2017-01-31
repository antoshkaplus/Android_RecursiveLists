package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/24/17.
 */
public class AncestorTraversal {

    interface Handler {
        // return true if want to continue
        boolean handle(Item ancestor);
    }

    BackendUser user;
    Handler handler;

    AncestorTraversal(BackendUser user, Handler handler) {
        this.user = user;
        this.handler = handler;
    }

    void traverse(Item item) {
        while (true) {
            if (item.getParentUuid().equals(user.getRootUuid())) return;
            Item parent = ofy().load().type(Item.class).parent(user).id(item.getParentUuid()).now();
            if (!handler.handle(parent)) return;
            item = parent;
        }
    }
}
