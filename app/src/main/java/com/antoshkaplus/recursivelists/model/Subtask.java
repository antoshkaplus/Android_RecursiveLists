package com.antoshkaplus.recursivelists.model;

import java.io.Serializable;

/**
 * Created by antoshkaplus on 1/24/17.
 */
public class Subtask implements Serializable {

    private int totalCount = 0;
    private int completedCount = 0;

    public Subtask() {}

    public Subtask(int completedCount, int totalCount) {
        this.completedCount = completedCount;
        this.totalCount = totalCount;
    }


    public void decCount() {
        --totalCount;
    }
    public void incCount() {
        ++totalCount;
    }

    public void incCompleted() {
        ++completedCount;
    }

    public void decCompleted() {
        --completedCount;
    }

    public boolean allCompleted() {
        return completedCount == totalCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public void add(Subtask subtask) {
        if (subtask == null) return;
        this.completedCount += subtask.completedCount;
        this.totalCount += subtask.totalCount;

    }
}
