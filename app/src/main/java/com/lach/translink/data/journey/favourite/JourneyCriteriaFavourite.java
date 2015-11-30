package com.lach.translink.data.journey.favourite;

import com.lach.translink.data.journey.history.JourneyCriteriaHistory;

/**
 * Represents a saved journey criteria which provides the user the ability to save and load
 * specific aspects of a journey. Any values null values are not appended to the search.
 */
public interface JourneyCriteriaFavourite extends JourneyCriteriaHistory {

    /**
     * The name of the criteria when searching for it.
     *
     * @return the associated name.
     */
    String getName();

    /**
     * Assigns a name to the criteria.
     *
     * @param name the associated name.
     */
    void setName(String name);

}