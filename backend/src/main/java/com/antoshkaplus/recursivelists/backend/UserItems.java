package com.antoshkaplus.recursivelists.backend;


import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchPlan;
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
    private Integer version;

    @Persistent(mappedBy = "userItems", defaultFetchGroup = "true", dependentElement = "true")
    private List<Item> items;

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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}