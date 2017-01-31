package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.Task;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/24/17.
 */
class UncompleteTask implements AncestorTraversal.Handler {

    int dbVersion;

    UncompleteTask(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public boolean handle(Item ancestor) {
        if (!ancestor.isTask()) return false;
        Task t = (Task) ancestor;
        boolean allCompleted = t.getSubtask().allCompleted();
        t.getSubtask().decCompleted();
        t.setDbVersion(dbVersion);

        if (allCompleted) {
            t.setCompleteDate(null);
            ofy().defer().save().entity(ancestor);
            return true;
        } else {
            // no reason to go farther
            ofy().defer().save().entity(ancestor);
            return false;
        }
    }

}
