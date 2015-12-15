package com.lach.translink.data.location.history;

import android.os.Parcelable;

import com.lach.translink.data.location.favourite.LocationFavourite;

import java.util.Date;

/**
 * Automatically saved locations which trigger when a location is resolved.
 */
public interface LocationHistory extends LocationFavourite, Parcelable {

    /**
     * A timestamp used to determine the last time this particular location was used.
     * <p>Used for sorting.</p>
     *
     * @return timestamp representing the last time this record was saved.
     */
    Date getLastUpdated();

    /**
     * Sets the date when this record was saved.
     * <p>Used for sorting.</p>
     *
     * @param lastUpdated when the record was updated.
     */
    void setLastUpdated(Date lastUpdated);

}