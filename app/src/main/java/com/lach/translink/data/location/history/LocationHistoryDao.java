package com.lach.translink.data.location.history;

import com.lach.common.log.Log;
import com.lach.translink.data.DbFlowDao;
import com.lach.translink.data.place.PlaceParser;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dao in charge of delivering {@link LocationHistoryDao} database results.
 */
public class LocationHistoryDao extends DbFlowDao<LocationHistory, LocationHistoryModel> {
    private static final String TAG = LocationHistoryDao.class.getSimpleName();

    @Override
    public Class<? extends Model> getModelClass() {
        return LocationHistoryModel.class;
    }

    @Override
    public Where<LocationHistoryModel> getAllRowsQuery() {
        return new Select()
                .from(LocationHistoryModel.class)
                .where()
                .limit(20)
                .orderBy(OrderBy.columns(LocationHistoryModel$Table.LASTUPDATED).descending());
    }

    @Override
    public LocationHistoryModel createModel() {
        return new LocationHistoryModel();
    }

    // Used to notify when content has been loaded.
    private HistoryLoadListener historyLoadListener;

    /**
     * Assigns a listener to trigger when history has been loaded via {@link #loadHistory}.
     *
     * @param historyLoadListener the fired listener.
     */
    public void setHistoryLoadListener(HistoryLoadListener historyLoadListener) {
        this.historyLoadListener = historyLoadListener;
    }

    /**
     * Removes any existing listeners to prevent memory leaks.
     */
    public void removeHistoryLoadListener() {
        this.historyLoadListener = null;
    }

    /**
     * Loads the location history, converts it to a list of addresses and triggers the assigned
     * listener with the results.
     */
    public void loadHistory() {
        TransactionManager.getInstance().addTransaction(new SelectListTransaction<>(
                getAllRowsQuery(),
                new TransactionListenerAdapter<List<LocationHistoryModel>>() {
                    @Override
                    public void onResultReceived(List<LocationHistoryModel> locationHistories) {
                        if (historyLoadListener == null) {
                            Log.warn(TAG, "historyLoadListener is not assigned");
                            return;
                        }


                        PlaceParser placeParser = new PlaceParser();

                        // Iterate through the history and create a results location list.
                        ArrayList<String> locationHistoryList = new ArrayList<>();
                        if (locationHistories != null) {
                            for (LocationHistory history : locationHistories) {
                                locationHistoryList.add(placeParser.prettyPrintPlace(history.getAddress()));
                            }
                        }

                        historyLoadListener.onHistoryLoaded(locationHistoryList);
                    }
                }));
    }

    /**
     * Inserts or updates an address depending on whether the record already exists. This is an
     * unfortunate requirement since the current database scheme doesn't use the REPLACE constraint.
     *
     * @param address the address to be inserted or updated.
     */
    public void insertOrUpdateAddress(String address) {
        // Check to see if the records already exists, if it does, we want to update the timestamp.
        LocationHistory query = new Select()
                .from(LocationHistoryModel.class)
                .where(
                        Condition.column(LocationHistoryModel$Table.ADDRESS).eq(address)
                )
                .limit(1)
                .orderBy(OrderBy.columns(LocationHistoryModel$Table.LASTUPDATED).descending())
                .querySingle();

        if (query != null) {
            // Since the record already exists, update it.
            Update.table(LocationHistoryModel.class)
                    .set(Condition.column(LocationHistoryModel$Table.LASTUPDATED).eq(new Date()))
                    .where(Condition.column(LocationHistoryModel$Table.ID).is(query.getId()))
                    .queryClose();

        } else {
            // Since the record doesn't exists, insert it.
            Insert.into(LocationHistoryModel.class)
                    .columns(LocationHistoryModel$Table.ADDRESS, LocationHistoryModel$Table.LASTUPDATED)
                    .values(address, new Date())
                    .queryClose();
        }
    }

    /**
     * A listener which is triggered when history content is loaded.
     */
    public interface HistoryLoadListener {
        /**
         * Triggered by history data being loaded from the database.
         *
         * @param locationHistoryList a list of addresses.
         */
        void onHistoryLoaded(ArrayList<String> locationHistoryList);
    }

}
