package com.antoshkaplus.recursivelists;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antoshkaplus.recursivelists.model.Item;
import com.antoshkaplus.recursivelists.model.ItemKind;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Anton.Logunov on 3/10/2015.
 */
public class ItemAdapter extends BaseAdapter {

    Context context;
    UUID parentId;
    List<Item> items = new ArrayList<>();

    public ItemAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.view_item, null);
        }
        Item item = (Item)getItem(position);
        TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
        textView.setText(item.title);
        int color = R.color.item;
        if (item.getItemKind() == ItemKind.Task) color = R.color.task;
        convertView.setBackground(context.getDrawable(color));
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    // this is not right
    // it's not unique value
    @Override
    public long getItemId(int position) {
        return items.get(position).id.getLeastSignificantBits();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    // TODO maybe make different layout for different things
//    @Override
//    public int getViewTypeCount() {
//        return 2;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        Item item = (Item)getItem(position);
//        return item.getItemKind().ordinal();
//    }
}