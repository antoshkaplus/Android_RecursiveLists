package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.BackendUser;
import com.antoshkaplus.recursivelists.backend.model.Task;

import java.util.UUID;

/**
 * Created by antoshkaplus on 2/12/17.
 */

public class Util {

    static Task toTask(Gtask gtask, BackendUser user, String parentUuid) {
        Task newTask = new Task();
        if (parentUuid == null) parentUuid = user.getRootUuid();
        newTask.setParentUuid(parentUuid);
        newTask.setOwner(user);
        // maybe that completed date is earlier
        newTask.setCreateDate(gtask.getUpdated());
        newTask.setUpdateDate(gtask.getUpdated());
        newTask.setCompleteDate(gtask.getCompleted());
        newTask.setUuid(UUID.randomUUID().toString());
        newTask.setTitle(gtask.getTitle());
        newTask.setDbVersion(user.getVersion());
        return newTask;
    }
}
