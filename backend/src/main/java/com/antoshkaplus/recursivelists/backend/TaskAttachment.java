
class TaskAttachment implements AncestorTraversal.Handler {

    private Date date;
    private int dbVersion;
 	private Subtask add;
	private Subtask addFirst;
	private BackendUser;
	
    public TaskAttachment(BackendUser backendUser, int dbVersion) {
        this.backendUser = backendUser;
		this.dbVersion = dbVersion;
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
		t.setDbVersion(dbVersion);
		ofy().defer().save().entity(t);
		
		if (!t.hasSubtasks() || t.isCompletedByDate() == t.isCompletedBySubtask()) {
			return false;
		}
		// we can't determine 'add' variable in detach/attach methods
		// because on attach if element is the first one the whole tree 
		// may go in either way complete/uncomplete
		if (!t.isCompletedByDate()) {
			t.setCompleteDate(date);
			add = new Subtask(1, 0);
		} else {
			t.setCompleteDate(null);
			add = new Subtask(-1, 0);
		}
		ofy().defer().save().entity(t);
		return true;
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
