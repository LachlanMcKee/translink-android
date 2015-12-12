package com.lach.translink.data;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.DeleteModelListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Arrays;
import java.util.List;

/**
 * A dao which implements the majority of the dao methods using the DbFlow database library.
 *
 * @param <TYPE>  the data type which is NOT a data driven object. It is usually an interface.
 * @param <MODEL> the data driven object which MUST implement the TYPE as well as the DbFlow model.
 */
public abstract class DbFlowDao<TYPE, MODEL extends Model> implements BaseDao<TYPE, MODEL> {

    @Override
    public long getRowCount() {
        return (new Select().count().from(getModelClass())).count();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TYPE get(long id) {
        return (TYPE) new Select().from(getModelClass()).byIds(id).querySingle();
    }

    @Override
    public Cursor getAllRowsCursor() {
        return getAllRowsQuery().query();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TYPE getItemFromCursor(Cursor cursor) {
        // Unfortunately since we cannot define a proper constraint in generics, we must have an unchecked cast.
        return (TYPE) SqlUtils.convertToModel(true, getModelClass(), cursor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<? extends TYPE> getAllRowsAsItems() {
        return (List<? extends TYPE>) getAllRowsQuery().queryList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertRows(boolean wait, List<TYPE> itemsToAdd) {
        TransactionManager instance = TransactionManager.getInstance();

        // We know 100% that the only implementation of LocationFavourite is LocationFavouriteModel.
        InsertModelTransaction<MODEL> transaction = new InsertModelTransaction<>(ProcessModelInfo.withModels((List<MODEL>) itemsToAdd));

        if (wait) {
            transaction.onExecute();
        } else {
            instance.addTransaction(transaction);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertRows(boolean wait, TYPE... itemsToAdd) {
        insertRows(wait, Arrays.asList(itemsToAdd));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteRows(boolean wait, List<TYPE> itemsToDelete) {
        TransactionManager instance = TransactionManager.getInstance();

        // We know 100% that the only implementation of LocationFavourite is LocationFavouriteModel.
        DeleteModelListTransaction<MODEL> transaction = new DeleteModelListTransaction(ProcessModelInfo.withModels((List<MODEL>) itemsToDelete));

        if (wait) {
            transaction.onExecute();
        } else {
            instance.addTransaction(transaction);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteRows(boolean wait, TYPE... itemsToDelete) {
        deleteRows(wait, Arrays.asList(itemsToDelete));
    }

    @Override
    public void deleteAllRows() {
        Delete.table(getModelClass());
    }

    /**
     * Unfortunate due to limitations with generics, the MODEL generic cannot be used as a constraint.
     *
     * @return the class type used for the model. It should be the same as MODEL!
     */
    public abstract Class<? extends Model> getModelClass();

    /**
     * Creates a DbFlow Where statement selector which allows {@link #getAllRowsCursor} and
     * {@link #getAllRowsAsItems} to obtain the data.
     *
     * @return a DbFlow selector.
     */
    public abstract Where<MODEL> getAllRowsQuery();
}
