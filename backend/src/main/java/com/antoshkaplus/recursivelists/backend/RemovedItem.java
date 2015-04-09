package com.antoshkaplus.recursivelists.backend;


import com.google.appengine.datanucleus.annotations.Unowned;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class RemovedItem {

    @Persistent
    @Unowned
    private Item item;

    @Persistent
    private Date deletionDate;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }
}
