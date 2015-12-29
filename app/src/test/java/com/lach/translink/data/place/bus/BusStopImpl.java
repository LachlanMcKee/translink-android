package com.lach.translink.data.place.bus;

import android.os.Parcel;
import android.os.Parcelable;

public class BusStopImpl implements BusStop {
    long id;
    String stationId;
    String description;
    double latitude;
    double longitude;

    @Override
    public long getId() {
        return id;
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return id + " " + description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(stationId);
        dest.writeString(description);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public BusStopImpl(long id) {
        this.id = id;
    }

    private BusStopImpl(Parcel in) {
        id = in.readLong();
        stationId = in.readString();
        description = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Parcelable.Creator<BusStopModel> CREATOR = new Parcelable.Creator<BusStopModel>() {
        public BusStopModel createFromParcel(Parcel source) {
            return new BusStopModel(source);
        }

        public BusStopModel[] newArray(int size) {
            return new BusStopModel[size];
        }
    };
}
