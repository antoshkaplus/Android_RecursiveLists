package com.antoshkaplus.recursivelists.backend;

import java.util.Date;

/**
 * Created by antoshkaplus on 2/8/17.
 */

public class Gtask {

    private String id;
    private Date updated;
    private String title;
    private Date completed;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }
}
