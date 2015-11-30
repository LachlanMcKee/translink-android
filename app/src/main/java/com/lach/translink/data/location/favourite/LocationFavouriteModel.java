package com.lach.translink.data.location.favourite;

import com.lach.translink.data.TranslinkDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = TranslinkDatabase.NAME, tableName = "LocationFavourite")
public class LocationFavouriteModel extends BaseModel implements LocationFavourite {

    @Column
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    @Unique(onUniqueConflict = ConflictAction.IGNORE)
    public String address;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }
}