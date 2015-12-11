package com.lach.translink.data.gocard;

import android.os.Parcel;

/**
 * Transaction journey which represents travel history between two locations.
 */
public class GoCardTransactionJourney implements GoCardTransaction {

    public final String startTime;
    public final String endTime;
    public final String fromLocation;
    public final String toLocation;
    public final String amount;

    public GoCardTransactionJourney(String startTime, String endTime, String fromLocation, String toLocation, String amount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.amount = amount;
    }

    // ---- Parcelable object methods
    private GoCardTransactionJourney(Parcel in) {
        startTime = in.readString();
        endTime = in.readString();
        fromLocation = in.readString();
        toLocation = in.readString();
        amount = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public GoCardTransactionJourney createFromParcel(Parcel in) {
            return new GoCardTransactionJourney(in);
        }

        public GoCardTransactionJourney[] newArray(int size) {
            return new GoCardTransactionJourney[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(fromLocation);
        dest.writeString(toLocation);
        dest.writeString(amount);
    }

    public String getTimeString() {
        String dateString = startTime;
        if (endTime != null && endTime.length() > 0) {
            dateString += " - " + endTime;
        }
        return dateString;
    }

}
