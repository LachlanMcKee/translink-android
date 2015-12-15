package com.lach.translink.data.location.history;

import android.os.Parcel;
import android.os.Parcelable;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(address);
        dest.writeLong(lastUpdated.getTime());
    }

    protected LocationHistoryModel() {
    }

    protected LocationHistoryModel(Parcel in) {
        id = in.readLong();
        address = in.readString();
        lastUpdated = new Date(in.readLong());
    }

    public static final Parcelable.Creator<LocationHistoryModel> CREATOR = new Parcelable.Creator<LocationHistoryModel>() {
        public LocationHistoryModel createFromParcel(Parcel source) {
            return new LocationHistoryModel(source);
        }

        public LocationHistoryModel[] newArray(int size) {
            return new LocationHistoryModel[size];
        }
    };
}