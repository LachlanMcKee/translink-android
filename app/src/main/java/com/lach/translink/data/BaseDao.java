package com.lach.translink.data;

import android.database.Cursor;

import java.util.List;

/**
 * Defines a dao which provides a mechanism to extract contact from a persisted source.
 * <p>
 * It is defined in an abstract manner to allow injection to avoid using the persisted source entirely.
 * </p>
 *
 * @param <TYPE>  the data type which is NOT a data driven object. It is usually an interface.
 * @param <MODEL> the data driven object which must extend the TYPE.
 *                Unfortunately due to java syntax, we cannot extend within the
 *                generic, otherwise it makes implementing further constraints later tricky!
 */
public interface BaseDao<TYPE, MODEL> {

    /**
     * Creates an instance of the model.
     *
     * @return the new instance.
     */
    MODEL createModel();

    /**
     * Gets the number of records.
     *
     * @return a count of the records.
     */
    long getRowCount();

    /**
     * Gets the record for the given id.
     *
     * @return the record which matches the id.
     */
    TYPE get(long id);

    /**
     * Creates a cursor which has access to all of the rows.
     *
     * @return a new cursor. Ensure that it is closed when required.
     */
    Cursor getAllRowsCursor();

    /**
     * Converts the values found at the current cursor position into a item type.
     *
     * @param cursor the cursor which is already pointing at the desired position.
     * @return a new instance which extends the item type class.
     */
    TYPE getItemFromCursor(Cursor cursor);

    /**
     * Obtains every row as a list of item types.
     * <p>Ensure this is called off the UI thread</p>
     *
     * @return list of item types.
     */
    List<? extends TYPE> getAllRowsAsItems();

    /**
     * Inserts a list of types.
     *
     * @param itemsToAdd the items to be added.
     */
    void insertRows(boolean wait, List<TYPE> itemsToAdd);

    /**
     * Inserts a list of types.
     *
     * @param itemsToAdd the items to be added.
     */
    void insertRows(boolean wait, TYPE... itemsToAdd);

    /**
     * Deletes a list of types.
     *
     * @param itemsToDelete the items to be deleted.
     */
    void deleteRows(boolean wait, List<TYPE> itemsToDelete);

    /**
     * Deletes a list of types.
     *
     * @param itemsToDelete the items to be deleted.
     */
    void deleteRows(boolean wait, TYPE... itemsToDelete);

    /**
     * Deletes every row in the persisted source.
     */
    void deleteAllRows();
}
