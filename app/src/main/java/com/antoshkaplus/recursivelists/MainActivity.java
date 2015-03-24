package com.antoshkaplus.recursivelists;

import android.app.Activity;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.antoshkaplus.recursivelists.dialog.AddStringDialog;
import com.antoshkaplus.recursivelists.dialog.EditStringDialog;
import com.antoshkaplus.recursivelists.dialog.RetryDialog;
import com.antoshkaplus.recursivelists.model.Item;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
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

    private final static String PREF_FIRST_LAUNCH = "first_launch";

    Menu optionsMenu;

    private int parentId;
    private int pressedPosition = 0;
    private boolean repositioning = false;
    private boolean contextMenuItemSelected = false;

    // those can be constants
    private int repositioningBarColor = Color.YELLOW;
    private int defaultBarColor = Color.LTGRAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstLaunchPopulation();
        setContentView(R.layout.activity_main);
        setActionBarColor(defaultBarColor);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            parentId = intent.getIntExtra(EXTRA_PARENT_ID, ROOT_ID);
        } else {
            parentId = savedInstanceState.getInt(EXTRA_PARENT_ID);
        }
        setActionBarTitle();
        getListView().setAdapter(new ItemAdapter(this, parentId));
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pressedPosition = position;
                getListView().setItemChecked(pressedPosition, true);
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

    private void firstLaunchPopulation()  {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(PREF_FIRST_LAUNCH)) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_FIRST_LAUNCH, true);
        editor.apply();
        // need to create multiple list probably better use json
        InputStream input = getResources().openRawResource(R.raw.default_data);
        try {
            JSONObject json = Utils.readDefaultData(input);
            DatabaseManager manager = new DatabaseManager(this);
            manager.clear();
            recursion(manager, ROOT_ID, json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void recursion(DatabaseManager manager, int parentId, JSONObject json) throws Exception {
        int order = manager.getChildrenCount(parentId);
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            String k = iter.next();
            Item item = new Item(k, order++, parentId);
            manager.addItem(item);
            recursion(manager, item.id, (JSONObject)json.get(k));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        optionsMenu = menu;
        return true;
    }

    private void setUndoRemovalVisible(boolean b) {
        MenuItem item = optionsMenu.findItem(R.id.action_undo_removal);
        item.setVisible(b);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_undo_removal);
        DatabaseManager manager = new DatabaseManager(this);
        try {
            item.setVisible(manager.hasRemovedItems());
        } catch (Exception ex) {
            item.setVisible(false);
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        contextMenuItemSelected = false;
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
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        // un selecting selected item
        if (!contextMenuItemSelected) updateListView();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        contextMenuItemSelected = true;
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
        updateListView();
        setUndoRemovalVisible(true);
    }

    void onMenuRemoveInner() {
        Item i = getItem(pressedPosition);
        DatabaseManager manager = new DatabaseManager(this);
        try {
            manager.deleteChildren(i);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // clear choices
        updateListView();
        setUndoRemovalVisible(true);
    }

    void onMenuEdit() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        EditStringDialog dialog = new EditStringDialog();
        Bundle args = new Bundle();
        args.putString(EditStringDialog.ARG_TITLE, "Edit:");
        args.putString(EditStringDialog.ARG_HINT, "Item");
        args.putString(EditStringDialog.ARG_TEXT, getItem(pressedPosition).title);
        dialog.setArguments(args);
        dialog.setEditStringDialogListener(new EditStringDialog.EditStringDialogListener() {
            @Override
            public void onEditStringDialogSuccess(CharSequence string) {
                DatabaseManager manager = new DatabaseManager(MainActivity.this);
                try {
                    Item item = manager.getItem(getItem(pressedPosition).id);
                    item.title = string.toString();
                    manager.updateItem(item);
                    updateListView();
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
        if (id == R.id.action_undo_removal) {
            DatabaseManager manager = new DatabaseManager(this);
            try {
                manager.undoLastRemoval();
                item.setVisible(manager.hasRemovedItems());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // retrive item from preferences
            // sometimes can be many of them

            updateListView();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_PARENT_ID, parentId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getListView().clearChoices();
        updateListView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (repositioning) {
            reposition(pressedPosition, position);
            endReposition();
            return;
        }
        updateListView();
        Intent intent = new Intent(this, MainActivity.class);
        Item item = getItem(position);
        intent.putExtra(EXTRA_PARENT_ID, item.id);
        startActivity(intent);
    }

    // should be called after every change in list structure
    // and when want to clear selections
    private void updateListView() {
        getListView().clearChoices();
        ItemAdapter adapter = (ItemAdapter)getListView().getAdapter();
        adapter.notifyDataSetChanged();
    }

    private ListView getListView() {
        return (ListView)findViewById(R.id.content);
    }

    private View getContainer() {
        return findViewById(R.id.container);
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
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        AddStringDialog dialog = new AddStringDialog();
        Bundle args = new Bundle();
        args.putString(AddStringDialog.ARG_TITLE, "Add new:");
        args.putString(AddStringDialog.ARG_HINT, "Item");
        dialog.setArguments(args);
        dialog.setAddStringDialogListener(new AddStringDialog.AddStringDialogListener() {
            @Override
            public void onAddStringDialogSuccess(CharSequence string) {
                // empty string
                if (string.toString().isEmpty()) {
                    // show dialog with on retry
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    final RetryDialog dialog = RetryDialog.newInstance("Error", "Item can't be empty string");
                    dialog.setRetryDialogListener(new RetryDialog.RetryDialogListener() {
                        @Override
                        public void onDialogCancel() {}

                        @Override
                        public void onDialogRetry() {
                            ShowAddNewDialog();
                        }
                    });
                    dialog.show(ft, "dialog");
                    return;
                }
                // item already exists
                DatabaseManager manager = new DatabaseManager(MainActivity.this);
                for (Item i : getItems()) {
                    if (i.title.contentEquals(string)) {
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        final RetryDialog dialog = RetryDialog.newInstance("Error", "Such item name already exists");
                        dialog.setRetryDialogListener(new RetryDialog.RetryDialogListener() {
                            @Override
                            public void onDialogCancel() {}

                            @Override
                            public void onDialogRetry() {
                                ShowAddNewDialog();
                            }
                        });
                        dialog.show(ft, "dialog");
                        return;
                    }
                }

                // need to add value at special location
                try {
                    manager.addItem(new Item(string.toString(), pressedPosition, parentId));
                    updateListView();
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

    private void reposition(int positionBefore, int positionAfter) {
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
        updateListView();
    }

    private void endReposition() {
        repositioning = false;
        getListView().clearChoices();
        setActionBarColor(defaultBarColor);
    }

    private void setActionBarColor(int color) {
        ActionBar bar = getActionBar();
        if (bar == null) return;
        bar.setBackgroundDrawable(new ColorDrawable(color));
    }

    private void setActionBarTitle() {
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

}
