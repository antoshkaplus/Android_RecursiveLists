package com.antoshkaplus.recursivelists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antoshkaplus.recursivelists.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton.Logunov on 3/10/2015.
 */
public class ItemAdapter extends BaseAdapter {

    Context context;
    int parentId;
    List<Item> items = new ArrayList<>();

    public ItemAdapter(Context context, int parentId) {
        this.context = context;
        this.parentId = parentId;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        try {
            DatabaseManager manager = new DatabaseManager(context);
            items = manager.getChildren(parentId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        Item item = (Item)getItem(position);
        TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
        textView.setText(item.title);
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    
}
