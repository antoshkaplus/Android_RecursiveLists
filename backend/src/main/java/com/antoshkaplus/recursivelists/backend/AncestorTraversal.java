package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/24/17.
 */
public class AncestorTraversal {

    public interface Handler {
        // return true if want to continue
        boolean handle(Item ancestor);
    }

    private BackendUser user;
    private Item item;

    public AncestorTraversal(BackendUser user, Item item) {
        this.user = user;
        this.item = item;
    }

    public void traverse(Handler handler) {
        while (true) {
            if (item.getParentUuid().equals(user.getRootUuid())) return;
            Item parent = ofy().load().type(Item.class).parent(user).id(item.getParentUuid()).now();
            if (!handler.handle(parent)) return;
            item = parent;
        }
    }
}
