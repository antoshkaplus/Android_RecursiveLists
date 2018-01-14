package com.antoshkaplus.recursivelists.backend.bean;

import com.antoshkaplus.recursivelists.backend.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 8/4/17.
 */

public class TaskList {

    private List<Task> items = new ArrayList<>();

    public TaskList() {}

    public TaskList(List<Task> items) {
        this.items = items;
    }

    public List<Task> getItems() {
        return items;
    }

    public void setItems(List<Task> items) {
        this.items = items;
    }

    public int size() {
        return items.size();
    }
}
