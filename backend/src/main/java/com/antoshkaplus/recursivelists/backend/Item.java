package com.antoshkaplus.recursivelists.backend;

import java.util.UUID;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Created by Anton.Logunov on 4/9/2015.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Item {
    @PrimaryKey
    @Persistent
    private UUID id;
    @Persistent
    private String title;
    @Persistent
    private Integer order;
    // should not forget to assign
    @Persistent
    private UUID parentId;


    public Item(UUID id, String title, int order, UUID parentId) {
        this.id = id;
        this.title = title;
        this.order = order;
        this.parentId = parentId;
    }

    // should be called by orm
    public Item() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

}

