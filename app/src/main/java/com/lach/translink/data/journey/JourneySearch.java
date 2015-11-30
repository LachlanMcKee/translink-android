package com.lach.translink.data.journey;

import com.lach.common.datatypes.Date;
import com.lach.common.datatypes.Time;
import com.lach.translink.data.location.ResolvedLocation;

import java.io.Serializable;

/**
 * @deprecated
 */
public class JourneySearch implements Serializable {

    private static final long serialVersionUID = -2530623016002220022L;

    /**
     * @deprecated
     */
    public enum TimeType {
        LeaveAfter("Leave After"), ArriveBefore("Arrive Before"), FirstTrip("First Trip"), LastTrip("Last Trip");

        private final String type;

        TimeType(String aType) {
            type = aType;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     * @deprecated
     */
    public enum TransportType {
        All("Any Vehicle"), Bus("Bus"), Train("Train"), Ferry("Ferry");

        private final String type;

        TransportType(String aType) {
            type = aType;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public final ResolvedLocation fromLocation;
    public final ResolvedLocation toLocation;
    public final TransportType transport;
    public final TimeType timeType;
    public final Time time;
    public final Date date;
    public String journeyName;

    public JourneySearch(ResolvedLocation fromLocation,
                         ResolvedLocation toLocation, TransportType transport,
                         TimeType timeType, Time time, Date date) {
        super();

        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.transport = transport;

        this.timeType = timeType;

        this.time = time;
        this.date = date;
    }

}