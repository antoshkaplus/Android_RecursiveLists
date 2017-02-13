package com.antoshkaplus.recursivelists.backend.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * Created by antoshkaplus on 1/27/17.
 */
@Entity
public class GtaskTrack {

    @Parent
    public Key<BackendUser> owner;

    @Id
    public String googleId;

    @Index
    @Load
    public Ref<Task> task;

}
