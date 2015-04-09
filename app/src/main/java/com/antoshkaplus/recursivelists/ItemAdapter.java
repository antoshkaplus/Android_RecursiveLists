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
import java.util.UUID;

/**
 * Created by Anton.Logunov on 3/10/2015.
 */
public class ItemAdapter extends BaseAdapter {

    Context context;
    UUID parentId;
    List<Item> items = new ArrayList<>();

    public ItemAdapter(Context context, UUID parentId) {
        this.context = context;
        this.parentId = parentId;
        ReloadItems();
    }

    private void ReloadItems() {
        try {
            ItemRepository manager = new ItemRepository(context);
            items = manager.getChildren(parentId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        ReloadItems();
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
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id.getLeastSignificantBits();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    
}
