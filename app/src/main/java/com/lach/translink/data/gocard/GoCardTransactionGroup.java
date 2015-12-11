package com.lach.translink.data.gocard;

import android.os.Parcel;

/**
 * Transaction group which is the heading for the any other transactions.
 */
public class GoCardTransactionGroup implements GoCardTransaction {

    public final String title;

    public GoCardTransactionGroup(String title) {
        this.title = title;
    }

    // ---- Parcelable object methods
    private GoCardTransactionGroup(Parcel in) {
        title = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public GoCardTransactionGroup createFromParcel(Parcel in) {
            return new GoCardTransactionGroup(in);
        }

        public GoCardTransactionGroup[] newArray(int size) {
            return new GoCardTransactionGroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
    }

}
