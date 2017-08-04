package com.antoshkaplus.recursivelists.backend.test;

import com.antoshkaplus.recursivelists.backend.bean.Gtask;
import com.antoshkaplus.recursivelists.backend.bean.VariantItem;
import com.antoshkaplus.recursivelists.backend.bean.VariantItemList;
import com.antoshkaplus.recursivelists.backend.model.Item;
import com.antoshkaplus.recursivelists.backend.model.Task;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;


/**
 * Created by antoshkaplus on 2/18/17.
 */

public class Util {

    public final static String SAMPLE_ROOT = "backend/data/test/";

    public static Sample getSample(int i) {
        return new Sample(SAMPLE_ROOT + String.format("sample_%1$d.json", i));

    }

    public static Scenario getScenario(int i) {
        return new Scenario(SAMPLE_ROOT + String.format("scenario_%1$d.json", i));
    }

    static Gtask toGtask(Task task, String id) {
        Gtask g = new Gtask();
        g.setId(id);
        g.setTitle(task.getTitle());
        g.setUpdated(task.getUpdateDate());
        g.setCompleted(task.getCompleteDate());
        return g;
    }

    public static Item itemWithUuid(String uuid) {
        Item i = new Item();
        i.setUuid(uuid);
        return i;
    }

    public static Item taskWithUuid(String uuid) {
        Item i = new Task();
        i.setUuid(uuid);
        return i;
    }

    public static int countEnabled(VariantItemList list) {
        return filter(having(on(Item.class).isEnabled()), extract(list.getVariantItems(), on(VariantItem.class).get())).size();
    }
}
