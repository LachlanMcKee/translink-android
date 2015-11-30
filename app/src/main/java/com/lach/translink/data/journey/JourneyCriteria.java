package com.lach.translink.data.journey;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * The journey searching criteria specified by a user.
 */
public class JourneyCriteria implements Parcelable {

    String fromAddress;
    String toAddress;
    JourneyTransport journeyTransport;
    JourneyTimeCriteria journeyTimeCriteria;
    Date time;

    @Nullable
    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(@Nullable String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Nullable
    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(@Nullable String toAddress) {
        this.toAddress = toAddress;
    }

    @Nullable
    public JourneyTransport getJourneyTransport() {
        return journeyTransport;
    }

    public void setJourneyTransport(@Nullable JourneyTransport journeyTransport) {
        this.journeyTransport = journeyTransport;
    }

    @Nullable
    public JourneyTimeCriteria getJourneyTimeCriteria() {
        return journeyTimeCriteria;
    }

    public void setJourneyTimeCriteria(@Nullable JourneyTimeCriteria journeyTimeCriteria) {
        this.journeyTimeCriteria = journeyTimeCriteria;
    }

    @Nullable
    public Date getTime() {
        return time;
    }

    public void setTime(@Nullable Date time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fromAddress);
        dest.writeString(this.toAddress);
        dest.writeInt(this.journeyTransport == null ? -1 : this.journeyTransport.ordinal());
        dest.writeInt(this.journeyTimeCriteria == null ? -1 : this.journeyTimeCriteria.ordinal());
        dest.writeLong(time != null ? time.getTime() : -1);
    }

    public JourneyCriteria() {
    }

    protected JourneyCriteria(Parcel in) {
        this.fromAddress = in.readString();
        this.toAddress = in.readString();
        int tmpJourneyTransport = in.readInt();
        this.journeyTransport = tmpJourneyTransport == -1 ? null : JourneyTransport.values()[tmpJourneyTransport];
        int tmpJourneyTimeCriteria = in.readInt();
        this.journeyTimeCriteria = tmpJourneyTimeCriteria == -1 ? null : JourneyTimeCriteria.values()[tmpJourneyTimeCriteria];
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
    }

    public static final Creator<JourneyCriteria> CREATOR = new Creator<JourneyCriteria>() {
        public JourneyCriteria createFromParcel(Parcel source) {
            return new JourneyCriteria(source);
        }

        public JourneyCriteria[] newArray(int size) {
            return new JourneyCriteria[size];
        }
    };
}