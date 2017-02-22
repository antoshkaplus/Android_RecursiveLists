package com.antoshkaplus.recursivelists.backend.test;

import com.antoshkaplus.recursivelists.backend.Gtask;
import com.antoshkaplus.recursivelists.backend.ItemsEndpoint;
import com.antoshkaplus.recursivelists.backend.model.Task;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;


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

}
