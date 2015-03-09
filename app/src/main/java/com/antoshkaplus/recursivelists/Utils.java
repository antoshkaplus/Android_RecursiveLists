package com.antoshkaplus.recursivelists;

import android.content.res.Resources;
import android.util.TypedValue;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by antoshkaplus on 10/30/14.
 */
public final class Utils {

    public static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static JSONObject readDefaultData(InputStream inputStream) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return new JSONObject(total.toString());
    }


}
