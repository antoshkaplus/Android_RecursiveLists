package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.AncestorTraversal;
import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.Subtask;
import com.antoshkaplus.recursivelists.backend.model.Task;

import java.util.Date;

import static com.googlecode.objectify.ObjectifyService.ofy;


public class SubtaskManagement implements AncestorTraversal.Handler {

    private Date date;
    private Subtask add;
	private Subtask addFirst;
	private BackendUser backendUser;

    // backendUser should have new db version
    public SubtaskManagement(BackendUser backendUser) {
        this.backendUser = backendUser;
    }
	
    @Override
    public boolean handle(Item ancestor) {
        if (!ancestor.isTask()) return false;
        Task t = (Task) ancestor;
		if (addFirst != null) {
			t.getSubtask().add(addFirst);
			addFirst = null;
		} else {
			t.getSubtask().add(add);
		}
		t.setDbVersion(backendUser.getVersion());
		ofy().defer().save().entity(t);
		
		if (!t.hasSubtasks() || isCompletedByDate(t) == isCompletedBySubtask(t)) {
			return false;
		}
		// we can't determine 'add' variable in detach/attach methods
		// because on attach if element is the first one the whole tree 
		// may go in either way complete/uncomplete
		if (!isCompletedByDate(t)) {
			t.setCompleteDate(date);
			add = new Subtask(1, 0);
		} else {
			t.setCompleteDate(null);
			add = new Subtask(-1, 0);
		}
		ofy().defer().save().entity(t);
		return true;
    }

    public void updateCompleteDate(Task task, Date completeDate) {
        // makes complete date different
        if (task.isCompleted() == (completeDate == null)) {
            if (task.isCompleted()) {
                addFirst = new Subtask(-1, 0);
            } else {
                addFirst = new Subtask(1, 0);
            }
            // have to update complete date before make
            task.setCompleteDate(completeDate);
            make(task);
        } else {
            task.setCompleteDate(completeDate);
        }
        task.setDbVersion(backendUser.getVersion());
        ofy().save().entity(task).now();
    }

	public void detach(Task task) {
		addFirst = new Subtask(task.isCompleted() ? -1 : 0, -1);
		make(task);
	}
	
	public void attach(Task task) {
		addFirst = new Subtask(task.isCompleted() ? 1 : 0, 1);
		make(task);
	}
	
	private void make(Task task) {
		date = task.getCompleteDate();
		AncestorTraversal ancTrav = new AncestorTraversal(backendUser, task);
		ancTrav.traverse(this);
	}
	
	private boolean isCompletedByDate(Task t) {
		return t.isCompleted();
	}
	
	private boolean isCompletedBySubtask(Task t) {
		Subtask s = t.getSubtask(); 
		return s.getTotalCount() == s.getCompletedCount();
	}
}
