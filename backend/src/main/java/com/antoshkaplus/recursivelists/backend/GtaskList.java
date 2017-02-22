package com.antoshkaplus.recursivelists.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 2/7/17.
 */

public class GtaskList {

    // like keep special Gtask guy... keep there all need attributes from Google Task API
    // better not to mix Item and shit and Gtask track with this.
    // separation of concerns. stuff like that.
    private List<Gtask> items = new ArrayList<>();
    private String parentUuid;

    public GtaskList() {}

    public GtaskList(List<Gtask> items, String parentUuid) {
        this.items = items;
        this.parentUuid = parentUuid;
    }

    public List<Gtask> getGtasks() {
        return items;
    }

    public void setGtasks(List<Gtask> items) {
        this.items = items;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }
}
