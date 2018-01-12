package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;

import java.util.List;
import java.util.Stack;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/12/18.
 */

public class Traversal<Info> {


    public static class Pair<Info> {
        boolean traverseChildren;
        Info info;
    }

    public interface Handler<Info> {
        Pair<Info> handle(Item item, Info parentInfo);
    }

    private static class P<Info> {
        Item item;
        Info info;

        P(Item item, Info info) {
            this.item = item;
            this.info = info;
        }
    }

    private BackendUser user;
    private Item item;
    private Stack<P<Info>> ps = new Stack<>();

    public Traversal(BackendUser user, Item item) {
        this.user = user;
        this.ps.push(new P(item, null));
    }

    public void traverse(Handler<Info> handler) {
        while (!ps.empty()) {
            P<Info> p = ps.pop();

            List<Item> items = ofy().load().type(Item.class).ancestor(user).filter("parentUuid== ", p.item.getUuid()).list();
            for (Item item : items) {
                Pair<Info> res = handler.handle(item, p.info);
                if (res.traverseChildren) {
                    ps.push(new P<Info>(item, res.info));
                }
            }
        }
    }
}
