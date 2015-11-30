package com.lach.translink.data.journey.history;

import com.lach.translink.data.journey.JourneyCriteria;

import java.util.Date;

/**
 * Represents a previously searched journeys which provides the user the ability to reload
 * the whole journey.
 */
public interface JourneyCriteriaHistory {

    /**
     * Unique identifier.
     *
     * @return a unique long.
     */
    long getId();

    /**
     * Date in which the history was created.
     * <p>Used for sorting.</p>
     *
     * @return the date criteria.
     */
    Date getDateCreated();

    /**
     * Sets the created date.
     * <p>Used for sorting.</p>
     *
     * @param dateCreated date created.
     */
    void setDateCreated(Date dateCreated);

    /**
     * The criteria being saved for later use.
     *
     * @return criteria used for journeys.
     */
    JourneyCriteria getJourneyCriteria();

    /**
     * Sets the criteria.
     *
     * @param criteria criteria used for journeys.
     */
    void setJourneyCriteria(JourneyCriteria criteria);

}