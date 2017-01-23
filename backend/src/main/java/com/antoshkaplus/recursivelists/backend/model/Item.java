package com.antoshkaplus.recursivelists.backend.model;


import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Index;


import java.util.Date;

/**
 * better use UUID as a Key
 * may need Integer order variable
 */
@Entity
public class Item {

    @Id
    private String uuid;

    private String title;

    @Parent
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    private Key<BackendUser> owner;

    // it's like deleted or not
    // after item gets disabled there is no way for it to change
    // otherwise as get enabled again
    // that way we always know when it got disabled
    private boolean disabled;
    private Date createDate;
    private Date updateDate;

    @Index
    private String parentUuid;

    // needed with index to get specific updates on synchronization
    // this is not item version it's database version of this item
    @Index
    private int dbVersion;

    private ItemKind kind;

    // should be called by orm
    public Item() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public Key<BackendUser> getOwner() {
        return owner;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }

    public void setDbVersion(int version) {
        this.dbVersion = version;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public ItemKind getKind() {
        return kind;
    }

    public void setKind(ItemKind kind) {
        this.kind = kind;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}

