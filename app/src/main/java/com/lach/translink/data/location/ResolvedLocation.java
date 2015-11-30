package com.lach.translink.data.location;

import android.os.Parcel;
import android.os.Parcelable;

import com.lach.translink.util.LocationUtil;

import java.io.Serializable;

/**
 * @deprecated
 */
public class ResolvedLocation implements Serializable, Parcelable {

    private static final long serialVersionUID = -3814740541578346000L;

    public final String displayAddress;
    private final String locationInformation;
    public boolean pinned;

    public ResolvedLocation(String displayAddress, String locationInformation) {
        super();
        this.displayAddress = displayAddress;
        this.locationInformation = locationInformation;
        this.pinned = false;
    }

    private ResolvedLocation(Parcel in) {
        this(in.readString(), in.readString());
        pinned = rBool(in);
    }

    private static boolean rBool(Parcel in) {
        return (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator CREATOR = new Creator() {
        public ResolvedLocation createFromParcel(Parcel in) {
            return new ResolvedLocation(in);
        }

        public ResolvedLocation[] newArray(int size) {
            return new ResolvedLocation[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayAddress);
        dest.writeString(locationInformation);
        dest.writeValue(pinned);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLocationInformation() {
        return LocationUtil.getLocationInformation(locationInformation);
    }

    @Override
    public String toString() {
        return "ResolvedLocation [displayAddress=" + displayAddress + ", locationInformation="
                + getLocationInformation() + ", pinned=" + pinned + "]";
    }

}
