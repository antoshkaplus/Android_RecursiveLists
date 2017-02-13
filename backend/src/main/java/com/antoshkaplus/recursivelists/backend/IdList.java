package com.antoshkaplus.recursivelists.backend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by antoshkaplus on 2/11/17.
 */

public class IdList {

    private List<String> idList = new ArrayList<>();

    public IdList() {}

    public IdList(List<String> idList) {
        this.idList = idList;
    }

    public List<String> getIds() {
        return idList;
    }

    public void setIds(List<String> idList) {
        this.idList = idList;
    }

}
