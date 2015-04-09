package com.antoshkaplus.recursivelists.backend;


import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

// going to store everything in one place
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserItems {
    @PrimaryKey
    @Persistent
    private String userId;
    @Persistent
    private List<Item> items;
    @Persistent
    private List<RemovedItem> removedItems;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<RemovedItem> getRemovedItems() {
        return removedItems;
    }

    public void setRemovedItems(List<RemovedItem> removedItems) {
        this.removedItems = removedItems;
    }

}
