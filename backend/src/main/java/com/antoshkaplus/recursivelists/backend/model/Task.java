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


    public Date getCompleteDate() {
        return completeDate;
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
}
