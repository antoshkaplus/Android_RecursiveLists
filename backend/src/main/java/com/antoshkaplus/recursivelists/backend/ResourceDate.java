package com.antoshkaplus.recursivelists.backend;

import java.util.Date;

/**
 * Created by antoshkaplus on 2/6/17.
 */

public class ResourceDate {

    public Date value;

    public ResourceDate() {}

    public ResourceDate(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }
}
