package com.lach.translink.data.location.favourite;

import com.lach.translink.data.DbFlowDao;
import com.lach.translink.data.location.ResolvedLocation;
import com.lach.translink.data.location.ResolvedLocationList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Dao in charge of delivering {@link LocationFavouriteDao} database results.
 */
public class LocationFavouriteDao extends DbFlowDao<LocationFavourite, LocationFavouriteModel> {

    @Override
    public LocationFavouriteModel createModel() {
        return new LocationFavouriteModel();
    }

    @Override
    public Class<LocationFavouriteModel> getModelClass() {
        return LocationFavouriteModel.class;
    }

    @Override
    public Where<LocationFavouriteModel> getAllRowsQuery() {
        return new Select()
                .from(LocationFavouriteModel.class)
                .orderBy(OrderBy.columns(LocationFavouriteModel$Table.ADDRESS).descending());
    }

    /**
     * A convenience method to save a favourite location without creating a model.
     *
     * @param address the address to save.
     */
    public void save(String address) {
        Insert.into(LocationFavouriteModel.class)
                .columns(LocationFavouriteModel$Table.ADDRESS)
                .values(address)
                .queryClose();
    }

    /**
     * Unfortunately the old mechanism for saving data did not use a good implementation.
     * <p/>
     * This must be safely transferred into the new database.
     *
     * @param savedLocations the previous persisted collection of saved locations.
     */
    public void migrateOldData(ResolvedLocationList savedLocations) {
        TransactionManager instance = TransactionManager.getInstance();

        //
        // Convert all the old implementations to the new one.
        //
        List<LocationFavouriteModel> locationFavouriteList = new ArrayList<>();
        for (ResolvedLocation savedLocation : savedLocations) {
            LocationFavouriteModel locationFavourite = new LocationFavouriteModel();
            locationFavourite.setAddress(savedLocation.displayAddress);

            locationFavouriteList.add(locationFavourite);
        }

        // Run an asynchronous task to insert all the new values.
        instance.addTransaction(new InsertModelTransaction<>(ProcessModelInfo.withModels(locationFavouriteList)));
    }
}
