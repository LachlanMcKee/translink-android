package com.lach.translink.ui.settings;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.lach.common.log.Log;
import com.lach.common.ui.BaseActivity;
import com.lach.common.ui.CursorRecyclerAdapter;
import com.lach.common.ui.view.ScaleAnimator;
import com.lach.translink.activities.R;
import com.lach.translink.data.BaseDao;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class CheckableListActivity<T> extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private View parent;
    private RecyclerView recyclerView;
    private final SparseBooleanArray mSelections = new SparseBooleanArray();

    private static final int LIST_DATA_LOADER = 0;

    private ScaleAnimator deleteScaler;

    private final List<T> previouslyDeletedItems = new ArrayList<>();

    private BaseDao<T, ?> listDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        listDao = getListDao();
        getSupportLoaderManager().initLoader(LIST_DATA_LOADER, null, this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_checkable_list);

        parent = findViewById(R.id.main_content);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(createAdapter());

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab_add);
        FloatingActionButton deleteButton = (FloatingActionButton) findViewById(R.id.fab_delete);

        View.OnClickListener addClickListener = getAddClickListener();
        if (addClickListener != null) {
            addButton.setOnClickListener(addClickListener);
            addButton.setVisibility(View.VISIBLE);
        }

        deleteScaler = new ScaleAnimator(deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItems();
            }
        });

        // Add the action bar back button.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract BaseDao<T, ?> getListDao();

    RecyclerView.Adapter<CheckboxViewHolder> createAdapter() {
        return new CheckboxAdapter(null, getRowLayoutId());
    }

    String getCheckboxText(T item) {
        return null;
    }

    protected abstract
    @LayoutRes int getRowLayoutId();

    protected abstract String getTypeDescription();

    View.OnClickListener getAddClickListener() {
        return null;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }

    protected CheckboxAdapter getAdapter() {
        return (CheckboxAdapter) recyclerView.getAdapter();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.debug("ABC", "onCreateLoader");
        switch (id) {
            case LIST_DATA_LOADER:
                return new CustomCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.debug("ABC", "onLoadFinished");
        getAdapter().changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getAdapter().changeCursor(null);
    }

    private void updateDeleteButtonVisibility() {
        boolean shouldBeVisible = false;

        for (int i = 0; i < mSelections.size(); i++) {
            if (mSelections.get(mSelections.keyAt(i))) {
                shouldBeVisible = true;
                break;
            }
        }

        if (shouldBeVisible) {
            deleteScaler.show();
        } else {
            deleteScaler.hide();
        }
    }

    public void reloadData() {
        getSupportLoaderManager().restartLoader(LIST_DATA_LOADER, null, CheckableListActivity.this);
    }

    private void deleteItems() {
        CheckboxAdapter adaptor = getAdapter();
        Cursor cursor = adaptor.getCursor();

        // Clear the previous deleted items.
        previouslyDeletedItems.clear();

        int currentListSize = adaptor.getItemCount();
        for (int listIndex = currentListSize; listIndex >= 0; listIndex--) {

            if (mSelections.get(listIndex)) {
                // Add to the deleted items so the user can undo.
                if (cursor.moveToPosition(listIndex)) {
                    T item = listDao.getItemFromCursor(cursor);
                    previouslyDeletedItems.add(item);
                }
            }
        }

        if (previouslyDeletedItems.size() == 0) {
            return;
        }

        // Delete items and update the list.
        listDao.deleteRows(true, previouslyDeletedItems);
        reloadData();

        // All the selected items have been deleted.
        mSelections.clear();
        updateDeleteButtonVisibility();

        if (Build.VERSION.SDK_INT >= 14) {
            showDeletedSnackbar();
        } else {
            // Delay showing the snackbar due to a gingerbread animation bug where the delete button is quite sluggish.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDeletedSnackbar();
                }
            }, 300L);
        }
    }

    private void showDeletedSnackbar() {
        // Give the user a chance to undo the delete.
        Snackbar.make(parent, "Selected " + getTypeDescription() + " deleted.", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (previouslyDeletedItems.size() > 0) {
                            // Re-add the items and update the list.
                            listDao.insertRows(true, previouslyDeletedItems);
                            reloadData();
                        }
                    }
                })
                .show();
    }

    public static class CustomCursorLoader extends CursorLoader {
        final WeakReference<CheckableListActivity> checkableListActivityWeakReference;
        final ForceLoadContentObserver mObserver;

        public CustomCursorLoader(CheckableListActivity activity) {
            super(activity);

            checkableListActivityWeakReference = new WeakReference<>(activity);

            mObserver = new ForceLoadContentObserver();
        }

        @Override
        public Cursor loadInBackground() {
            CheckableListActivity checkableListActivity = checkableListActivityWeakReference.get();
            if (checkableListActivity == null) {
                return null;
            }

            Cursor cursor = checkableListActivity.listDao.getAllRowsCursor();
            checkableListActivityWeakReference.clear();

            if (cursor != null) {
                // Ensure the cursor window is filled
                //cursor.getCount();
                cursor.registerContentObserver(mObserver);
            }
            return cursor;
        }
    }

    public class CheckboxAdapter extends CursorRecyclerAdapter<CheckboxViewHolder> {
        private final int layoutId;

        public CheckboxAdapter(Cursor cursor, @LayoutRes int layoutId) {
            super(cursor);

            this.layoutId = layoutId;
        }

        @Override
        public CheckboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new CheckboxViewHolder(view, R.id.saved_location_check_box);
        }

        @Override
        public void onBindViewHolder(CheckboxViewHolder holder, Cursor cursor) {
            T item = listDao.getItemFromCursor(cursor);

            String text = getCheckboxText(item);
            holder.mCheckbox.setText(text);
            holder.mCheckbox.setChecked(mSelections.get(cursor.getPosition()));
            Log.debug("ABC", "Binding");
        }

        @Override
        public String getIdColumnName() {
            return "id";
        }
    }

    public class CheckboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final CheckBox mCheckbox;

        public CheckboxViewHolder(View itemView, @IdRes int checkboxId) {
            super(itemView);

            mCheckbox = (CheckBox) itemView.findViewById(checkboxId);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            boolean checked = !mCheckbox.isChecked();
            mCheckbox.setChecked(checked);
            mSelections.put(getAdapterPosition(), checked);

            updateDeleteButtonVisibility();
        }
    }

}
