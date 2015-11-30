package com.lach.common.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @deprecated
 */
public class Time implements Serializable, Parcelable {

    private static final long serialVersionUID = 148132749583919159L;
    private int hour;
    private int minute;
    private AmPm amPm;

    public Time() {
        this(-1, -1);
    }

    public Time(int hour24, int minute) {

        if ((hour24 == -1) && (minute == -1)) {
            Calendar c = Calendar.getInstance();
            hour24 = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        this.hour = hour24;
        setAmPm(AmPm.AM);
        if (hour24 >= 12) {
            setAmPm(AmPm.PM);
        }

        this.minute = minute;
    }

    public Time(Time time) {
        setHour(time.getHour());
        setMinute(time.getMinute());
        setAmPm(time.getAmPm());
    }

    public int getHour() {
        return hour;
    }

    public int getHourMeridian() {
        if (hour > 12) {
            return hour - 12;
        }
        if (hour == 0) {
            return 12;
        }
        return hour;
    }

    private void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    private void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public String toString() {

        return "" + getHourMeridian() + ":"
                + ((Integer.valueOf(minute).toString().length() == 1) ? "0" + minute : minute)
                + getAmPm().toString();
    }

    private void setAmPm(AmPm amPm) {
        this.amPm = amPm;
    }

    public AmPm getAmPm() {
        return amPm;
    }

    public enum AmPm {
        AM, PM
    }

    // ---- Parcellable object methods

    private Time(Parcel in) {
        setHour(in.readInt());
        setMinute(in.readInt());

        if (in.dataSize() > 2) {
            amPm = AmPm.valueOf(in.readString());
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator CREATOR = new Creator() {
        public Time createFromParcel(Parcel in) {
            return new Time(in);
        }

        public Time[] newArray(int size) {
            return new Time[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getHour());
        dest.writeInt(getMinute());

        AmPm amPm = getAmPm();
        if (amPm != null) {
            dest.writeString(amPm.name());
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }
}
