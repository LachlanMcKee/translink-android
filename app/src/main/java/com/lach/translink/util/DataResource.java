package com.lach.translink.util;

import android.content.Context;

import com.lach.common.datatypes.Time;
import com.lach.common.log.Log;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneySearch;
import com.lach.translink.data.journey.JourneySearchList;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.data.location.ResolvedLocationList;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;

import javax.inject.Inject;

public class DataResource {
    private static final String TAG = "DataResource";

    private static final String LOCATION_FAVOURITES = "FavoriteLocationsNew";
    private static final String SAVED_JOURNEYS = "SavedJourneysNew";
    private static final String JOURNEY_HISTORY = "JourneysHistoryNew";

    @Inject
    JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    @Inject
    JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;

    @Inject
    LocationFavouriteDao locationFavouriteDao;

    public void migrate(TranslinkApplication app) {
        app.getDataComponent().inject(this);

        ResolvedLocationList savedLocations = load(app, LOCATION_FAVOURITES);
        JourneySearchList journeysList = load(app, SAVED_JOURNEYS);
        JourneySearchList journeysHistoryList = load(app, JOURNEY_HISTORY);

        if (savedLocations == null && journeysList == null && journeysHistoryList == null) {
            Log.debug(TAG, "No data to migrate");
            return;
        }

        if (savedLocations != null && savedLocations.size() > 0) {
            Log.debug(TAG, "Migrating savedLocations. count : " + savedLocations.size());

            locationFavouriteDao.migrateOldData(savedLocations);
            delete(app, LOCATION_FAVOURITES);
        }

        if (journeysList != null && journeysList.size() > 0) {
            Log.debug(TAG, "Migrating savedLocations. count : " + journeysList.size());

            journeyCriteriaFavouriteDao.migrateOldData(journeysList);
            delete(app, SAVED_JOURNEYS);
        }

        if (journeysHistoryList != null && journeysHistoryList.size() > 0) {
            Log.debug(TAG, "Migrating savedLocations. count : " + journeysHistoryList.size());

            journeyCriteriaHistoryDao.migrateOldData(journeysHistoryList);
            delete(app, JOURNEY_HISTORY);
        }
    }

    public static JourneyCriteria convertJourneySearchToCriteria(JourneySearch journeySearch) {
        JourneyCriteria criteria = new JourneyCriteria();

        if (journeySearch.fromLocation != null) {
            criteria.setFromAddress(journeySearch.fromLocation.displayAddress);
        }
        if (journeySearch.toLocation != null) {
            criteria.setToAddress(journeySearch.toLocation.displayAddress);
        }

        // Map the old enum to the new one.
        if (journeySearch.transport != null) {
            JourneyTransport transport = JourneyTransport.All;
            switch (journeySearch.transport) {
                case Bus:
                    transport = JourneyTransport.Bus;
                    break;

                case Train:
                    transport = JourneyTransport.Train;
                    break;

                case Ferry:
                    transport = JourneyTransport.Ferry;
                    break;
            }
            criteria.setJourneyTransport(transport);
        }

        // Map the old enum to the new one.
        if (journeySearch.timeType != null) {
            JourneyTimeCriteria timeCriteria = JourneyTimeCriteria.LeaveAfter;
            switch (journeySearch.timeType) {
                case ArriveBefore:
                    timeCriteria = JourneyTimeCriteria.ArriveBefore;
                    break;

                case FirstTrip:
                    timeCriteria = JourneyTimeCriteria.FirstTrip;
                    break;

                case LastTrip:
                    timeCriteria = JourneyTimeCriteria.LastTrip;
                    break;
            }
            criteria.setJourneyTimeCriteria(timeCriteria);
        }

        Time searchTime = journeySearch.time;
        if (searchTime != null) {
            Calendar criteriaTimeCalendar = Calendar.getInstance();

            // The time component is the only part that matters.
            criteriaTimeCalendar.set(Calendar.HOUR_OF_DAY, searchTime.getHour());
            criteriaTimeCalendar.set(Calendar.MINUTE, searchTime.getMinute());
            criteriaTimeCalendar.set(Calendar.AM_PM, searchTime.getAmPm() == Time.AmPm.AM ? Calendar.AM : Calendar.PM);

            criteria.setTime(criteriaTimeCalendar.getTime());
        }

        return criteria;
    }

    private static <T> T load(Context context, String name) {
        T data = null;
        try {

            if (context != null) {
                FileInputStream fis = context.openFileInput(name);

                // Next, create an object that can read from that file.
                ObjectInputStream inStream = new ObjectInputStream(fis);

                // Retrieve the Serializable object.
                //noinspection unchecked
                data = (T) inStream.readObject();

                inStream.close();
            }

        } catch (Exception ignored) {

        }
        return data;
    }

    private static void delete(Context context, String name) {
        context.deleteFile(name);
    }

}