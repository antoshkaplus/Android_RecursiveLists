package com.antoshkaplus.recursivelists.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.UUID;


/**
 * Created by antoshkaplus on 3/9/17.
 */
@DatabaseTable(tableName = Task.TABLE_NAME)
public class Task extends Item {

    public static final String TABLE_NAME = "task";

    @DatabaseField
    private Date completeDate;
    @DatabaseField
    private int priority;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Subtask subtask;
    @DatabaseField
    private boolean current = false;

    public Task() {
    }

    public Task(String title, int order, UUID parentId, ItemState state) {
        super(title, order, parentId, state);
    }

    public Task(String title, int order, UUID parentId) {
        super(title, order, parentId, ItemState.Local);
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

    @Override
    public ItemKind getItemKind() {
        return ItemKind.Task;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public boolean isCurrent() {
        return current;
    }
}
