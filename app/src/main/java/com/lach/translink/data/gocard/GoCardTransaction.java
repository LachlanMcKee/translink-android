package com.lach.translink.data.gocard;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Transaction details of a customers go-card, each transaction represents a trip, or a top up event.
 */
public class GoCardTransaction implements Parcelable {

    public String date;
    public String startTime;
    public String endTime;
    public String fromLocation;
    public String toLocation;
    public String amount;
    public boolean isTopUp;

    public GoCardTransaction() {

    }

    // ---- Parcellable object methods
    private GoCardTransaction(Parcel in) {
        date = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        fromLocation = in.readString();
        toLocation = in.readString();
        amount = in.readString();
        isTopUp = in.readInt() == 1;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator CREATOR = new Creator() {
        public GoCardTransaction createFromParcel(Parcel in) {
            return new GoCardTransaction(in);
        }

        public GoCardTransaction[] newArray(int size) {
            return new GoCardTransaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(fromLocation);
        dest.writeString(toLocation);
        dest.writeString(amount);
        dest.writeInt(isTopUp ? 1 : 0);
    }

    public String getTimeString() {
        String dateString = startTime;
        if (!TextUtils.isEmpty(endTime)) {
            dateString += " - " + endTime;
        }

        return dateString;
    }
}
