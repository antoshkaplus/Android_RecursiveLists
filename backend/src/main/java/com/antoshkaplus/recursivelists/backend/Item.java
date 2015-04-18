package com.antoshkaplus.recursivelists.backend;

import com.google.appengine.api.datastore.Key;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * better use UUID as a Key
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Item {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    @Index
    private String id;
    @Persistent
    private String title;
    @Persistent
    private Integer order;
    // should not forget to assign
    @Persistent
    private String parentId;

    @Persistent
    private UserItems userItems;

    @Persistent
    private Date deletionDate;



    public Item(String id, String parentId, String title, int order) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.order = order;
    }

    // should be called by orm
    public Item() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public UserItems getUserItems() {
        return userItems;
    }

    public void setUserItems(UserItems userItems) {
        this.userItems = userItems;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

}

