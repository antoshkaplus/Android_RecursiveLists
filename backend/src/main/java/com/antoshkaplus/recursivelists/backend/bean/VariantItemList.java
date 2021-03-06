package com.antoshkaplus.recursivelists.backend.bean;

import com.antoshkaplus.recursivelists.backend.model.Item;

import java.util.ArrayList;
import java.util.List;

import ch.lambdaj.function.convert.Converter;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;

/**
 * Created by antoshkaplus on 9/5/16.
 */
public class VariantItemList {

    private List<VariantItem> items = new ArrayList<>();


    public VariantItemList() {}


    public VariantItemList(List<VariantItem> items) {
        this.items = items;
    }

    public static VariantItemList createFromItems(List<Item> items) {
        return new VariantItemList(convert(items, new Converter<Item, VariantItem>() {
            @Override
            public VariantItem convert(Item from) {
                return VariantItem.create(from);
            }
        }));
    }

    public List<VariantItem> getVariantItems() {
        return items;
    }

    public List<Item> convertToItems() {
        return extract(items, on(VariantItem.class).get());
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


