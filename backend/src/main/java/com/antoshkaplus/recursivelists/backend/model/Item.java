package com.antoshkaplus.recursivelists.backend.model;


import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Index;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private Key<BackendUser> owner;

    // it's like deleted or not
    // after item gets disabled there is no way for it to change
    // otherwise as get enabled again
    // that way we always know when it got disabled
    @Index
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

    // could be hidden somewhere in Util, as doesn't correspond to context
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isValid() {
        return  uuid != null &&
                title != null &&
                owner != null &&
                createDate != null &&
                updateDate != null &&
                !updateDate.before(createDate) &&
                parentUuid != null;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getValidityReport() {
        StringBuilder report = new StringBuilder();

        List<String> lines = new ArrayList<>();
        if (uuid == null) lines.add("uuid");
        if (title == null) lines.add("title");
        if (owner == null) lines.add("owner");
        if (createDate == null) lines.add("createDate");
        if (updateDate == null) lines.add("updateDate");
        if (parentUuid == null) lines.add("parentUuid");
        if (!lines.isEmpty()) {
            report = new StringBuilder("Missing fields:")
                        .append(StringUtils.join(lines, ","))
                        .append(".");
        }
        if (updateDate != null && createDate != null && updateDate.before(createDate)) {
            report.append(String.format("updateDate %s earlier than createDate %s.", updateDate, createDate));
        }
        if (report.length() == 0) {
            report.append("Instance is Valid.");
        }
        return report.toString();
    }



    // should be called by orm
    public Item() {
        kind = ItemKind.Item;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isTask() {
        return kind == ItemKind.Task;
    }

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

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<BackendUser> getOwner() {
        return owner;
    }

    public void setOwner(BackendUser owner) {
        this.owner = owner.getKey();
    }

    public void setDbVersion(int version) {
        this.dbVersion = version;
        this.updateDate = new Date();
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
        this.updateDate = createDate;
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

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isEnabled() { return !disabled; }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}

