package com.lach.common.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @deprecated
 */
public class Date implements Serializable, Parcelable {

    private static final long serialVersionUID = -8213727001426580136L;

    private int day;
    private int month;
    private int year;

    public Date() {
        Calendar c = Calendar.getInstance();

        setYear(c.get(Calendar.YEAR));
        setMonth(c.get(Calendar.MONTH) + 1);
        setDay(c.get(Calendar.DAY_OF_MONTH));
    }

    public Date(Date date) {
        setYear(date.getYear());
        setMonth(date.getMonth());
        setDay(date.getDay());
    }

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    private void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    private void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    private void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return day + "/" + padZero(month) + "/" + year + " 12:00:00 AM";
    }

    public String toDisplayString() {
        return padZero(day) + "/" + padZero(month) + "/" + year;
    }

    private String padZero(int value) {
        String valueStr = Integer.valueOf(value).toString();
        return ((valueStr.length() == 1) ? ("0" + valueStr) : valueStr);
    }

    // ---- Parcellable object methods

    private Date(Parcel in) {
        setDay(in.readInt());
        setMonth(in.readInt());
        setYear(in.readInt());
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator CREATOR = new Creator() {
        public Date createFromParcel(Parcel in) {
            return new Date(in);
        }

        public Date[] newArray(int size) {
            return new Date[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getDay());
        dest.writeInt(getMonth());
        dest.writeInt(getYear());

    }

    @Override
    public int describeContents() {

        return 0;
    }

}
