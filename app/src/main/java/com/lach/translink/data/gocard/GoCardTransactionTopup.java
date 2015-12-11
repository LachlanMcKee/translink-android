package com.lach.translink.data.gocard;

import android.os.Parcel;

/**
 * Transaction which describes a top-up event.
 */
public class GoCardTransactionTopUp implements GoCardTransaction {

    public final String time;
    public final String description;
    public final String oldAmount;
    public final String newAmount;

    public GoCardTransactionTopUp(String time, String description, String oldAmount, String newAmount) {
        this.time = time;
        this.description = description;
        this.oldAmount = oldAmount;
        this.newAmount = newAmount;
    }

    // ---- Parcelable object methods
    private GoCardTransactionTopUp(Parcel in) {
        time = in.readString();
        description = in.readString();
        oldAmount = in.readString();
        newAmount = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public GoCardTransactionTopUp createFromParcel(Parcel in) {
            return new GoCardTransactionTopUp(in);
        }

        public GoCardTransactionTopUp[] newArray(int size) {
            return new GoCardTransactionTopUp[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(time);
        dest.writeString(description);
        dest.writeString(oldAmount);
        dest.writeString(newAmount);
    }

}
