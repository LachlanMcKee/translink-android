package com.lach.translink.data.journey;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The journey searching criteria specified by a user.
 */
public class JourneyCriteria implements Parcelable {

    private String fromAddress;
    private String toAddress;
    private List<JourneyTransport> journeyTransport;
    private JourneyTimeCriteria journeyTimeCriteria;
    private Date time;

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
    public List<JourneyTransport> getJourneyTransport() {
        return journeyTransport;
    }

    public void setJourneyTransport(@Nullable List<JourneyTransport> journeyTransport) {
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

        if (this.journeyTransport != null) {
            dest.writeInt(this.journeyTransport.size());
            for (JourneyTransport jt : this.journeyTransport) {
                dest.writeInt(jt.ordinal());
            }
        } else {
            dest.writeInt(-1);
        }

        dest.writeInt(this.journeyTimeCriteria == null ? -1 : this.journeyTimeCriteria.ordinal());
        dest.writeLong(time != null ? time.getTime() : -1);
    }

    public JourneyCriteria() {
    }

    private JourneyCriteria(Parcel in) {
        this.fromAddress = in.readString();
        this.toAddress = in.readString();

        int journeyTransportArrayLength = in.readInt();
        if (journeyTransportArrayLength > 0) {
            this.journeyTransport = new ArrayList<>();

            for (int i = 0; i < journeyTransportArrayLength; i++) {
                this.journeyTransport.add(JourneyTransport.values()[in.readInt()]);
            }
        }

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