package mbanje.kurt.todo.ui;

import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mbanje.kurt.todo.R;
import mbanje.kurt.todo.Todo;
import mbanje.kurt.todo.TodoProvider;
import mbanje.kurt.todo.TodoTable;
import mbanje.kurt.todo.provider.TodoHelper;
import mbanje.kurt.todo.utils.Settings;
import mbanje.kurt.todo.utils.TodoUtils;
import mbanje.kurt.todo.widget.ProgressView;


public class TodoActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String TAG = TodoActivity.class.getSimpleName();
    private TextView completed, databaseFile;
    private ProgressView progressBar;
    private TodoAdapter adapter;
    private List<Todo> list = new ArrayList<Todo>();
    private ListView listView;
    private SwitchCompat toggle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        completed = (TextView) findViewById(R.id.todo_list_percent);
        listView = (ListView) findViewById(R.id.todo_list_items);
        databaseFile = (TextView) findViewById(R.id.database);
        toggle = (SwitchCompat) findViewById(R.id.todo_switch_db);

//        View empty =  View.inflate(this,R.layout.todo_empty,null);
        View empty = getLayoutInflater().inflate(R.layout.todo_empty, null, false);
        addContentView(empty, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setEmptyView(empty);

        progressBar = (ProgressView) findViewById(R.id.todo_list_progress);
        adapter = new TodoAdapter(this, list);
        listView.setAdapter(adapter);
        listView.setEmptyView(empty);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "clicked: " + adapter.getItem(position));
            }
        });


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                adapter.updateSelections(position, checked);
                setTitle(mode);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteSelectedItems();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                adapter.clearSelections();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        findViewById(R.id.todo_list_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });


        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String dbfile = "testdb";
                if (isChecked) {
                    dbfile = "base";
                }
                databaseFile.setText(Html.fromHtml(getString(R.string.todo_db_file, dbfile)));
                updateDbFile(dbfile);
            }
        });


        getLoaderManager().restartLoader(0, null, this);
        String dbPath = TodoUtils.getDataBasePath(this);
        try {
            TodoUtils.copyDbAsset(this, "testdb", dbPath);
        } catch (IOException e) {
            Log.e(TAG, "error", e);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        String dbname = getDatabaseName();
        boolean checked = dbname.equalsIgnoreCase("base");
        databaseFile.setText(Html.fromHtml(getString(R.string.todo_db_file, dbname)));
        toggle.setChecked(checked);
    }

    /**
     * sets the actionbar title when we're in multi select mode
     *
     * @param mode
     */
    private void setTitle(ActionMode mode) {
        final int checkedCount = listView.getCheckedItemCount();
        switch (checkedCount) {
            case 0:
                mode.setSubtitle(null);
                break;
            case 1:
                mode.setSubtitle(getString(R.string.todo_one_selection));
                break;
            default:
                mode.setSubtitle(getString(R.string.todo_multi_selection, checkedCount));
                break;
        }
    }


    /**
     * deletes the selected items
     */
    private void deleteSelectedItems() {
        SparseBooleanArray positions = listView.getCheckedItemPositions();
        List<Todo> deletions = new ArrayList<Todo>();
        //theres a bug in the multiselect mode on some devices so if you have one item in listView.getCheckedItemPositions() delete it
        if (positions.size() == 1) {
            deletions.add(adapter.getItem(positions.keyAt(0)));
        } else {
            //create the list of items to delete from list
            for (int i = 0; i < positions.size(); i++) {
                int key = positions.keyAt(i);
                if (positions.get(key)) {
                    deletions.add(adapter.getItem(key));
                }
            }
        }
        for (Todo item : deletions) { //if we actually have any items to delete then delete them from db
            TodoHelper.deleteTodo(getContentResolver(), item);
        }
        adapter.clearSelections();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, TodoTable.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            List<Todo> items = TodoTable.getValues(cursor, false);
            Log.d(TAG,"onLoadFinished() [items:"+items.size()+"]");
            updateView(items);
        }
        Log.d(TAG, "adapter updated");
    }


    private void updateView(List<Todo> items) {
        list.clear();
        float percent;
        int marked = 0;
        for (Todo item : items) {
            if (item.completed) {
                marked += 1;
            }
            list.add(item);
        }
        int currentProgress = 0;
        if (items.size() > 0) {
            percent = ((float) marked / (float) items.size()) * 100f;
            currentProgress = Math.round(percent);
            progressBar.setPercent(currentProgress);
            completed.setText(Html.fromHtml(String.format("%02d", currentProgress) + getString(R.string.todo_list_progress_percent)));
        } else {
            progressBar.setPercent(currentProgress);
            completed.setText("");
        }
        Log.d(TAG, "[marked:" + marked + "][total:" + items.size() + "] [progress:" + currentProgress + "]");
        adapter.notifyDataSetChanged();
        Log.d(TAG, "view updated");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private boolean validInput(EditText label, EditText description) {
        label.setError(null);
        description.setError(null);
        if (TextUtils.isEmpty(label.getText())) {
            label.setError(getString(R.string.todo_title_required));
            label.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(description.getText())) {
            description.setError(getString(R.string.todo_desc_required));
            description.requestFocus();
            return false;
        }
        return true;
    }

    private void addItem() {
        final Dialog dialog = new Dialog(this, R.style.SaveDialogTheme);
        View view = View.inflate(this, R.layout.todo_add, null);
        final EditText label = (EditText) view.findViewById(R.id.todo_dialog_label);
        final EditText descr = (EditText) view.findViewById(R.id.todo_dialog_description);
        view.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validInput(label, descr)) {
                    Todo item = new Todo();
                    item.label = label.getText().toString();
                    item.description = descr.getText().toString();
                    if (TodoHelper.createTodo(getContentResolver(), item) != null) {
                        Toast.makeText(TodoActivity.this, getString(R.string.todo_item_added), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Log.d(TAG, "added new todo entry...");
                    }
                }

            }
        });
        dialog.setContentView(view);
        dialog.show();
    }


    private void updateDbFile(String file) {
        Settings settings = new Settings(this);
        settings.setString(Settings.PREF_DATABASE, file);
        TodoProvider provider = TodoTable.getContentProvider(this);
        if (provider != null) {
            provider.reset(file);
        }
    }

    private String getDatabaseName() {
        TodoProvider provider = TodoTable.getContentProvider(this);
        if (provider != null) {
            return provider.getDatabaseHelper().getDatabaseName();
        }
        return null;
    }



}
