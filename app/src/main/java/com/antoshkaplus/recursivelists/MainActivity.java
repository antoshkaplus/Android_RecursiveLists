package com.antoshkaplus.recursivelists;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    // should think about good naming of those variables
    public final static String EXTRA_PARENT_ID = "ExtraParentId";

    private final static int MENU_ADD_NEW = 0;
    private final static int MENU_REMOVE = 1;
    private final static int MENU_REMOVE_INNER = 5;
    private final static int MENU_EDIT = 2;
    private final static int MENU_REPOSITION = 3;

    private final static int ROOT_ID  = -1;

    private int parentId;
    private int pressedPosition = 0;
    private boolean repositioning = false;

    // those can be constants
    private int repositioningBarColor = Color.YELLOW;
    private int defaultBarColor = Color.LTGRAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActionBarColor(defaultBarColor);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            parentId = intent.getIntExtra(EXTRA_PARENT_ID, ROOT_ID);
        } else {
            parentId = savedInstanceState.getInt(EXTRA_PARENT_ID);
        }
        setActionBarTitle();
        resetAdapter();
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pressedPosition = position;
                return false;
            }
        });
        View container = findViewById(R.id.container);
        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pressedPosition = getListView().getAdapter().getCount();
                ShowAddNewDialog();
                return true;
            }
        });
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repositioning) {
                    reposition(pressedPosition, getItemCount());
                    endReposition();
                }
            }
        });
        registerForContextMenu(container);
        registerForContextMenu(getListView());
    }

    ListView getListView() {
        return (ListView)findViewById(R.id.content);
    }

    View getContainer() {
        return findViewById(R.id.container);
    }

    void setListAdapter(ListAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    // should be called after every change
    void resetAdapter() {
        ArrayAdapter<Item> adapter = new ArrayAdapter<Item>(
                this, android.R.layout.simple_list_item_1,
                android.R.id.text1, getItems());
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // no menu for now
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if ((v == getListView() || v == getContainer()) && menu.size() == 0) {
            menu.add(0, MENU_ADD_NEW, 0, "Add New");
            if (getItemCount() != pressedPosition) {
                // click on item // and not blank?
                menu.add(0, MENU_REMOVE, 0, "Remove");
                menu.add(0, MENU_REMOVE_INNER, 0, "Remove Inner");
                menu.add(0, MENU_EDIT, 0, "Edit");
                if (getItemCount() > 1) menu.add(0, MENU_REPOSITION, 0, "Reposition");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // can also change color of items around
        switch (item.getItemId()) {
            case MENU_ADD_NEW: {
                ShowAddNewDialog();
                break;
            }
            case MENU_EDIT: {
                onMenuEdit();
                break;
            }
            case MENU_REMOVE: {
                onMenuRemove();
                break;
            }
            case MENU_REMOVE_INNER: {
                onMenuRemoveInner();
                break;
            }
            case MENU_REPOSITION: {
                setActionBarColor(repositioningBarColor);
                getListView().setSelection(pressedPosition);
                repositioning = true;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown menu item identifier");
            }
        }
        return super.onContextItemSelected(item);
    }

    void onMenuRemove() {
        Item i = getItem(pressedPosition);
        DatabaseManager manager = new DatabaseManager(this);
        try {
            manager.deleteItem(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        resetAdapter();
    }

    void onMenuRemoveInner() {
        Item i = getItem(pressedPosition);
        DatabaseManager manager = new DatabaseManager(this);
        try {
            manager.deleteChildren(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        resetAdapter();
    }

    void onMenuEdit() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        EditStringDialog dialog = new EditStringDialog();
        Bundle args = new Bundle();
        args.putString(EditStringDialog.ARG_TITLE, "Edit:");
        args.putString(EditStringDialog.ARG_HINT, "Item");
        dialog.setArguments(args);
        dialog.setEditStringDialogListener(new EditStringDialog.EditStringDialogListener() {
            @Override
            public void onEditStringDialogSuccess(CharSequence string) {
                DatabaseManager manager = new DatabaseManager(MainActivity.this);
                try {
                    Item item = manager.getItem(getItem(pressedPosition).id);
                    item.title = string.toString();
                    manager.updateItem(item);
                    resetAdapter();
                    // update current list
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                manager.close();
            }
            @Override
            public void onEditStringDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Item getItem(int position) {
        return (Item)getListView().getAdapter().getItem(position);
    }

    private List<Item> getItems() {
        DatabaseManager manager = new DatabaseManager(this);
        List<Item> items = new ArrayList<>();
        try {
            items = manager.getChildren(parentId);
        } catch (Exception ex) {
            // just use finally
            ex.printStackTrace();
        }
        manager.close();
        return items;
    }

    private int getItemCount() {
        return getListView().getAdapter().getCount();
    }

    private void ShowAddNewDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        AddStringDialog dialog = new AddStringDialog();
        Bundle args = new Bundle();
        args.putString(AddStringDialog.ARG_TITLE, "Add new:");
        args.putString(AddStringDialog.ARG_HINT, "Item");
        dialog.setArguments(args);
        dialog.setAddStringDialogListener(new AddStringDialog.AddStringDialogListener() {
            @Override
            public void onAddStringDialogSuccess(CharSequence string) {
                // need to add value at special location
                DatabaseManager manager = new DatabaseManager(MainActivity.this);
                try {
                    manager.addItem(new Item(string.toString(), pressedPosition, parentId));
                    resetAdapter();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                manager.close();
            }
            @Override
            public void onAddStringDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }

    void reposition(int positionBefore, int positionAfter) {
        List<Item> items = getItems();
        Item item = items.remove(positionBefore);
        if (positionBefore <= positionAfter) --positionAfter;
        items.add(positionAfter, item);
        for (int i = 0; i < items.size(); ++i) {
            items.get(i).order = i;
        }
        DatabaseManager manager = new DatabaseManager(MainActivity.this);
        try {
            manager.updateItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        resetAdapter();
    }

    void endReposition() {
        repositioning = false;
        setActionBarColor(defaultBarColor);
    }

    void setActionBarColor(int color) {
        ActionBar bar = getActionBar();
        if (bar == null) return;
        bar.setBackgroundDrawable(new ColorDrawable(color));
    }

    void setActionBarTitle() {
        ActionBar bar = getActionBar();
        if (bar == null) return;
        String title = "Root";
        if (parentId != ROOT_ID) {
            DatabaseManager manager = new DatabaseManager(this);
            try {
                Item item = manager.getItem(parentId);
                title = item.title;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        bar.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PARENT_ID, parentId);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (repositioning) {
            reposition(pressedPosition, position);
            endReposition();
            return;
        }
        resetAdapter();
        Intent intent = new Intent(this, MainActivity.class);
        Item item = getItem(position);
        intent.putExtra(EXTRA_PARENT_ID, item.id);
        startActivity(intent);
    }



}
