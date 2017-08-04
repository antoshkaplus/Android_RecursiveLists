package com.antoshkaplus.recursivelists.backend.model;

import com.googlecode.objectify.annotation.Subclass;

import java.util.Date;

/**
 * Created by antoshkaplus on 9/5/16.
 */
@Subclass(index = true)
public class Task extends Item {

    private Date completeDate;
    private int priority;
    private Subtask subtask;
    private boolean current = false;

    public Task() {
        setKind(ItemKind.Task);
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public boolean isCompleted() {
        return completeDate != null;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Subtask getSubtask() {
        if (subtask == null) subtask = new Subtask();
        return subtask;
    }

    public void setSubtask(Subtask subtask) {
        this.subtask = subtask;
    }

    public boolean hasSubtasks() {
        return subtask != null && subtask.getTotalCount() > 0;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isCurrent() {
        return current;
    }
}
