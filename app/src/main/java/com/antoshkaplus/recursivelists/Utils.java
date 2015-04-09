package com.antoshkaplus.recursivelists;

import android.content.res.Resources;
import android.util.TypedValue;

import com.antoshkaplus.recursivelists.backend.userItemsApi.model.UUID;
import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.RemovedItem;
import com.google.api.client.util.DateTime;

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

    public static UUID toBackendUUID(java.util.UUID uuid) {
        UUID apiUUID = new UUID();
        apiUUID.setLeastSignificantBits(uuid.getLeastSignificantBits());
        apiUUID.setMostSignificantBits(uuid.getMostSignificantBits());
        return apiUUID;
    }


    public static com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item toBackendItem(Item item) {
        com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item apiItem = new com.antoshkaplus.recursivelists.backend.userItemsApi.model.Item();
        apiItem.setId(toBackendUUID(item.id));
        apiItem.setTitle(item.title);
        apiItem.setOrder(item.order);
        apiItem.setParentId(toBackendUUID(item.parentId));
        return apiItem;
    }

    public static com.antoshkaplus.recursivelists.backend.userItemsApi.model.RemovedItem toBackendRemovedItem(RemovedItem removedItem) {
        com.antoshkaplus.recursivelists.backend.userItemsApi.model.RemovedItem apiRemovedItem = new com.antoshkaplus.recursivelists.backend.userItemsApi.model.RemovedItem();
        apiRemovedItem.setDeletionDate(new DateTime(removedItem.deletionDate));
        apiRemovedItem.setItem(toBackendItem(removedItem.item));
        return apiRemovedItem;
    }
}
