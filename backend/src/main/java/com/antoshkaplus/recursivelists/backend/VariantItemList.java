package com.antoshkaplus.recursivelists.backend;

import com.antoshkaplus.recursivelists.backend.model.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by antoshkaplus on 9/5/16.
 */
public class VariantItemList {

    private List<VariantItem> items = new ArrayList<>();


    public VariantItemList() {}


    public VariantItemList(List<VariantItem> items) {
        this.items = items;
    }

    static VariantItemList createFromItems(List<Item> items) {
        VariantItemList list = new VariantItemList();
        list.items = items.stream().map(VariantItem::create).collect(Collectors.toList());
        return list;
    }

    public List<VariantItem> getVariantItems() {
        return items;
    }

    public List<Item> convertToItems() {
        return items.stream().map(VariantItem::get).collect(Collectors.toList());
    }

    public void add(Item item) {
        items.add(item == null ? null : VariantItem.create(item));
    }

    public void setVariantItems(List<VariantItem> items) {
        this.items = items;
    }

    public int size() {
        return items.size();
    }
}


