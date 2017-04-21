package com.antoshkaplus.recursivelists.activity;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.antoshkaplus.fly.dialog.RetryDialog;
import com.antoshkaplus.recursivelists.BuildConfig;
import com.antoshkaplus.recursivelists.CredentialFactory;
import com.antoshkaplus.recursivelists.ItemAdapter;
import com.antoshkaplus.recursivelists.R;
import com.antoshkaplus.recursivelists.SyncTask;
import com.antoshkaplus.recursivelists.Utils;
import com.antoshkaplus.recursivelists.backend.itemsApi.ItemsApi;
import com.antoshkaplus.recursivelists.db.ItemRepository;
import com.antoshkaplus.recursivelists.dialog.AddItemDialog;
import com.antoshkaplus.recursivelists.dialog.EditStringDialog;
import com.antoshkaplus.fly.dialog.OkDialog;
import com.antoshkaplus.recursivelists.model.Item;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import org.json.JSONObject;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private final static int REQUEST_ACCOUNT_PICKER = 11;

    // should think about good naming of those variables
    // have UUID as string inside
    public final static String EXTRA_PARENT_ID = "ExtraParentId";
    private final static String PREF_FIRST_LAUNCH = "first_launch";

    Menu optionsMenu;

    private UUID rootId;
    private UUID parentId;
    private int pressedPosition = 0;
    private boolean repositioning = false;
    private boolean movingIn = false;
    private boolean contextMenuItemSelected = false;
    private boolean syncing = false;

    private boolean onCreateSuccess = true;

    // those can be constants
    private int repositionBarColor = Color.YELLOW;
    private int moveInBarColor = Color.GREEN;
    private int defaultBarColor = Color.LTGRAY;

    // should always be ordered
    private List<Item> items = new ArrayList<>();
    private ItemRepository repository;

    SharedPreferences settings;

    GoogleAccountCredential credential;

    SharedPreferences.OnSharedPreferenceChangeListener settingsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.pref__account__key))) {
                onAccountChanged();
            }
        }
    };

    // populate first launch data inside database
    // define parentId
    // assign context menus and events
    // assign repository and items
    // populate listview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateSuccess = true;
        setContentView(R.layout.activity_main);
        getListView().setAdapter(new ItemAdapter(this, items));
        setActionBarColor(defaultBarColor);
        // should assign before doing any operations with settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        settings.registerOnSharedPreferenceChangeListener(settingsListener);
        String account = retrieveAccount();
        credential = CredentialFactory.create(this, account);
        if (account == null) {
            chooseAccount();
        } else {
            repository = new ItemRepository(this, account);
            try {
                rootId = repository.getRootId();
                if (savedInstanceState == null) {
                    Intent intent = getIntent();
                    parentId = (UUID) intent.getSerializableExtra(EXTRA_PARENT_ID);
                    if (parentId == null) parentId = rootId;
                } else {
                    parentId = (UUID) savedInstanceState.getSerializable(EXTRA_PARENT_ID);
                }
                // need to know parentId before initializing actionBarTitle
                setActionBarTitle();
            } catch (SQLException ex) {
                ex.printStackTrace();
                onCreateSuccess = false;
            }
            loadItems();
            onItemsChanged();
        }
        prepareViews();
    }

    @Override
    protected void onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        super.onDestroy();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        UUID parentId = (UUID)intent.getSerializableExtra(EXTRA_PARENT_ID);
        if (parentId == null) {
            // relaunching application
            return;
        }
        this.parentId = parentId;
        setActionBarTitle();
        loadItems();
        onItemsChanged();
    }

    // setting up listeners and context menus
    private void prepareViews() {
        getListView().setAdapter(new ItemAdapter(this, items));
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
                showAddNewDialog();
                return true;
            }
        });
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repositioning) {
                    reposition(pressedPosition, items.size());
                    endReposition();
                }
            }
        });
        registerForContextMenu(container);
        registerForContextMenu(getListView());
    }

    // should be called after user chooses account
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
            List<Item> result = new ArrayList<>();
            recursion(result, rootId, json);
            repository.addItemList(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        loadItems();
        onItemsChanged();
    }

    private void recursion(List<Item> result, UUID parentId, JSONObject json) throws Exception {
        int order = 0;
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            String k = iter.next();
            Item item = new Item(k, order++, parentId);
            result.add(item);
            recursion(result, item.id, (JSONObject)json.get(k));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (syncing) {
            MenuItem item = menu.findItem(R.id.action_sync);
            item.setActionView(R.layout.actionbar__indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_undo_removal);
        // can be not initialized...
        ItemRepository manager = repository;
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
            menu.add(0, R.string.ctx__add_new, 0, getString(R.string.ctx__add_new));
            if (items.size() != pressedPosition) {
                // click on item // and not blank?
                menu.add(0, R.string.ctx__remove, 0, getString(R.string.ctx__remove));
                menu.add(0, R.string.ctx__remove_inner, 0, getString(R.string.ctx__remove_inner));
                menu.add(0, R.string.ctx__edit, 0, getString(R.string.ctx__edit));
                if (items.size() > 1) menu.add(0, R.string.ctx__reposition, 0, getString(R.string.ctx__reposition));
                if (!parentId.equals(rootId)) {
                    menu.add(0, R.string.ctx__move_out, 0, getString(R.string.ctx__move_out));
                }
                if (items.size() > 1) menu.add(0, R.string.ctx__move_in, 0, getString(R.string.ctx__move_in));
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
            case R.string.ctx__add_new: {
                showAddNewDialog();
                return true;
            }
            case R.string.ctx__edit: {
                showEditDialog();
                return true;
            }
            case R.string.ctx__remove: {
                onMenuRemove();
                return true;
            }
            case R.string.ctx__remove_inner: {
                onMenuRemoveInner();
                return true;
            }
            case R.string.ctx__reposition: {
                setActionBarColor(repositionBarColor);
                repositioning = true;
                return true;
            }
            case R.string.ctx__move_out: {
                moveOut(pressedPosition);
                return true;
            }
            case R.string.ctx__move_in: {
                setActionBarColor(moveInBarColor);
                movingIn = true;
                return true;
            }
            default: {
                throw new IllegalArgumentException("Unknown menu item identifier");
            }
        }
    }

    private void moveOut(int position) {
        Item moveItem = items.remove(pressedPosition);
        for (int i = moveItem.order; i < items.size(); ++i) {
            items.get(i).order = i;
        }
        try {
            Item currentParent = repository.getItem(parentId);
            moveItem.parentId = currentParent.parentId;
            repository.updateItem(moveItem);
            repository.updateAllItems(items.subList(moveItem.order, items.size()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        updateListView();
    }


    private void onMenuRemove() {
        Item item = items.remove(pressedPosition);
        for (int i = item.order; i < items.size(); ++i) {
            items.get(i).order = i;
        }
        try {
            repository.deleteItem(item);
            repository.updateAllItems(items.subList(item.order, items.size()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        updateListView();
        // to allow undo deletion
        invalidateOptionsMenu();
    }

    private void onMenuRemoveInner() {
        Item i = items.get(pressedPosition);
        try {
            repository.deleteChildren(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        updateListView();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_sync:
                if (!syncing) {
                    onSync();
                }
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.action_undo_removal:
                try {
                    repository.undoLastRemoval();
                    if (repository.getChildrenCount(parentId) != items.size()) {
                        loadItems();
                        onItemsChanged();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void onSync() {
        SyncTask task = new SyncTask(new ItemRepository(this, retrieveAccount()), CreateUserItemsEndpoint());
        task.setListener(new SyncTask.Listener() {
            // should've used async task instead. no need for runOnUiThread in that case
            @Override
            public void onStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncing = true;
                        invalidateOptionsMenu();
                    }
                });
            }

            @Override
            public void onFinish(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncing = false;
                        invalidateOptionsMenu();
                        FragmentManager mgr = getFragmentManager();
                        if (!success) {
                            // show negative message


                            OkDialog.newInstance(
                                    getString(R.string.dialog__sync_failure__title),
                                    getString(R.string.dialog__sync_failure__text)).show(mgr, "failure");
                        } else {
                            // show positive message
                            OkDialog.newInstance(
                                    getString(R.string.dialog__sync_success__title),
                                    getString(R.string.dialog__sync_success__text)).show(mgr, "success");
                            loadItems();
                            onItemsChanged();
                        }
                    }
                });
            }
        });
        new Thread(task).start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_PARENT_ID, parentId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!onCreateSuccess) {
            FragmentManager mgr = getFragmentManager();
            OkDialog.newInstance(
                    getString(R.string.dialog__error__title),
                    getString(R.string.dialog__unexpected_error__text)).show(mgr, "success");
        }
        // will try to fill in items here
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateListView();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (repositioning) {
            reposition(pressedPosition, position);
            endReposition();
            return;
        }
        if (movingIn) {
            moveIn(pressedPosition, position);
            endMoveIn();
            return;
        }
        // clearing any selections on current activity
        // to return on cleared one
        updateListView();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_PARENT_ID, items.get(position).id);
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

    // returns items for current activity from repository
    private void loadItems() {
        try {
            items.clear();
            // already sorted
            items.addAll(repository.getChildren(parentId));
        } catch (Exception ex) {
            // just use finally
            ex.printStackTrace();
        }
    }

    void showEditDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        EditStringDialog dialog = new EditStringDialog();
        Bundle args = new Bundle();
        args.putString(EditStringDialog.ARG_TITLE, "Edit:");
        args.putString(EditStringDialog.ARG_TEXT, items.get(pressedPosition).title);
        dialog.setArguments(args);
        dialog.setEditStringDialogListener(new EditStringDialog.EditStringDialogListener() {
            @Override
            public void onEditStringDialogSuccess(CharSequence string) {
                RetryDialog.RetryDialogListener listener = new RetryDialog.RetryDialogListener() {
                    @Override
                    public void onDialogCancel() { }
                    @Override
                    public void onDialogRetry() {
                        showEditDialog();
                    }
                };
                // empty string
                if (string.toString().isEmpty()) {
                    // show dialog with on retry
                    showRetryDialog(
                            getString(R.string.dialog__empty__title),
                            getString(R.string.dialog__empty__text),
                            listener);
                    return;
                }
                // item already exists
                boolean exists = false;
                for (Item i : items) {
                    if (i.order != pressedPosition && i.title.contentEquals(string)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    showRetryDialog(
                            getString(R.string.dialog__exists__title),
                            getString(R.string.dialog__exists__text),
                            listener);
                    return;
                }

                try {
                    Item item = items.get(pressedPosition);
                    item.title = string.toString();
                    repository.updateItem(item);
                    updateListView();
                    // update current list
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            @Override
            public void onEditStringDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }

    private void showAddNewDialog() {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        AddItemDialog dialog = new AddItemDialog();
        Bundle args = new Bundle();
        args.putString(AddItemDialog.ARG_TITLE, getString(R.string.dialog__add_title));
        dialog.setArguments(args);
        dialog.setAddItemDialogListener(new AddItemDialog.AddItemDialogListener() {
            @Override
            public void onAddItemDialogSuccess(Item result) {
                RetryDialog.RetryDialogListener listener = new RetryDialog.RetryDialogListener() {
                    @Override
                    public void onDialogCancel() { }
                    @Override
                    public void onDialogRetry() {
                        showAddNewDialog();
                    }
                };
                // empty string
                if (result.title.toString().isEmpty()) {
                    // show dialog with on retry
                    showRetryDialog(
                            getString(R.string.dialog__empty__title),
                            getString(R.string.dialog__empty__text),
                            listener);
                    return;
                }
                // item already exists
                boolean exists = false;
                for (Item i : items) {
                    if (i.title.contentEquals(result.title)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    showRetryDialog(
                            getString(R.string.dialog__exists__title),
                            getString(R.string.dialog__exists__text),
                            listener);
                    return;
                }
                addNewItem(result);
                getListView().clearChoices();
            }
            @Override
            public void onAddItemDialogCancel() {}
        });
        dialog.show(ft, "dialog");
    }

    private void showRetryDialog(String title, String text, RetryDialog.RetryDialogListener listener) {
        RetryDialog dialog = (RetryDialog)getFragmentManager().findFragmentByTag("retry_dialog");
        if (dialog == null) {
            dialog = RetryDialog.newInstance(title, text);
        }
        dialog.setRetryDialogListener(listener);
        dialog.show(getFragmentManager(), "retry_dialog");
    }

    private void addNewItem(Item item) {
        item.order = pressedPosition;
        item.parentId = parentId;
        try {
            items.add(item.order, item);
            for (int i = item.order+1; i < items.size(); ++i) {
                items.get(i).order = i;
            }
            repository.addItem(item);
            repository.updateAllItems(items.subList(item.order+1, items.size()));
            onItemsChanged();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void reposition(int positionBefore, int positionAfter) {
        // position after can be equal to items count
        if (items.size() == positionAfter) {
            --positionAfter;
        }
        // did a better way of doing it
        Item item = items.remove(positionBefore);
        int from, to;
        items.add(positionAfter, item);
        if (positionBefore < positionAfter) {
            from = positionBefore;
            to = positionAfter+1;
        } else {
            from = positionAfter;
            to = positionBefore+1;
        }
        for (int i = from; i < to; ++i) {
            items.get(i).order = i;
        }
        try {
            repository.updateAllItems(items.subList(from, to));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        onItemsChanged();
    }

    private void moveIn(int positionWhich, int positionWhere) {
        Item which = items.get(positionWhich);
        Item where = items.get(positionWhere);
        which.parentId = where.id;
        // need to update order in current list
        // and make last one in where
        items.remove(positionWhich);
        for (int i = positionWhich; i < items.size(); ++i) {
            items.get(i).order = i;
        }
        try {
            repository.updateAllItems(items.subList(positionWhich, items.size()));
            which.order = repository.getChildrenCount(where.id);
            repository.updateItem(which);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        onItemsChanged();
    }

    private void endReposition() {
        repositioning = false;
        getListView().clearChoices();
        setActionBarColor(defaultBarColor);
    }

    private void endMoveIn() {
        movingIn = false;
        getListView().clearChoices();
        setActionBarColor(defaultBarColor);
    }

    private void setActionBarColor(int color) {
        ActionBar bar = getActionBar();
        if (bar == null) return;
        bar.setBackgroundDrawable(new ColorDrawable(color));
    }

    // need repository to be set
    private void setActionBarTitle() {
        ActionBar bar = getActionBar();
        if (bar == null) return;
        String title = getString(R.string.title_root);
        if (rootId != null && !parentId.equals(rootId)) {
            try {
                Item item = repository.getItem(parentId);
                title = item.title;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        bar.setTitle(title);
    }

    private ItemsApi CreateUserItemsEndpoint() {
        ItemsApi.Builder builder = new ItemsApi.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), credential);
        builder.setRootUrl(BuildConfig.HOST);
        builder.setHttpRequestInitializer(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) {
                credential.initialize(httpRequest);
                httpRequest.setConnectTimeout(20 * 1000);  // 3 seconds connect timeout
                httpRequest.setReadTimeout(20 * 1000);  // 3 seconds read timeout
            }
        });
        // TODO may need this line
        //builder.setApplicationName("antoshkaplus-recursivelists");
        return builder.build();
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                        editor.putString(getString(R.string.pref__account__key), accountName);
                        editor.apply();
                        onAccountChanged();
                        firstLaunchPopulation();
                    }
                }
                break;
        }
    }

    private void onAccountChanged() {
        String account = retrieveAccount();
        credential.setSelectedAccountName(account);
        repository = new ItemRepository(this, account);
        try {
            parentId = rootId = repository.getRootId();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        loadItems();
        onItemsChanged();
    }

    private String retrieveAccount() {
        return settings.getString(getString(R.string.pref__account__key), null);
    }

    private void onItemsChanged() {
        ItemAdapter adapter = (ItemAdapter)getListView().getAdapter();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (rootId == null || parentId.equals(rootId)) {
            super.onBackPressed();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        // outer parent id
        UUID id = null;
        try {
            id = repository.getItem(parentId).parentId;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        intent.putExtra(EXTRA_PARENT_ID, id);
        startActivity(intent);
    }
}
