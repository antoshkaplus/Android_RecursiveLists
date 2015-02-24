package com.antoshkaplus.recursivelists;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by antoshkaplus on 10/30/14.
 */
public final class Utils {

    public static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

}
