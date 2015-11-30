package com.lach.translink.data.location.history;

import com.lach.translink.data.TranslinkDatabase;
import com.lach.translink.data.location.favourite.LocationFavouriteModel;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.Date;

@Table(databaseName = TranslinkDatabase.NAME, tableName = "LocationHistory")
public class LocationHistoryModel extends LocationFavouriteModel implements LocationHistory {

    @Column
    @NotNull
    Date lastUpdated;

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}