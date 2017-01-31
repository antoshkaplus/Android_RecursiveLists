package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.Task;

import java.util.Date;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by antoshkaplus on 1/24/17.
 *
 * Class name isn't very descriptive. Requires changes.
 */
class CompleteTask implements AncestorTraversal.Handler {

    Date date;
    int dbVersion;

    CompleteTask(Date date, int dbVersion) {
        this.date = date;
        this.dbVersion = dbVersion;
    }

    @Override
    public boolean handle(Item ancestor) {
        if (!ancestor.isTask()) return false;
        Task t = (Task) ancestor;
        t.getSubtask().incCompleted();
        t.setDbVersion(dbVersion);
        if (t.getSubtask().allCompleted()) {
            t.setCompleteDate(date);
            ofy().defer().save().entity(ancestor);
            return true;
        } else {
            // no reason to go farther
            ofy().defer().save().entity(ancestor);
            return false;
        }
    }
}
