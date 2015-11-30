package com.lach.translink.data.gocard;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Summary details of a customers go-card
 */
public class GoCardDetails implements Parcelable {

    public String issueDate;
    public String expiryDate;
    public String balance;
    public String passengerType;
    public String balanceTime;

    public GoCardDetails() {

    }

    // ---- Parcellable object methods
    private GoCardDetails(Parcel in) {
        issueDate = in.readString();
        expiryDate = in.readString();
        balance = in.readString();
        passengerType = in.readString();
        balanceTime = in.readString();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator CREATOR = new Creator() {
        public GoCardDetails createFromParcel(Parcel in) {
            return new GoCardDetails(in);
        }

        public GoCardDetails[] newArray(int size) {
            return new GoCardDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(issueDate);
        dest.writeString(expiryDate);
        dest.writeString(balance);
        dest.writeString(passengerType);
        dest.writeString(balanceTime);
    }

}