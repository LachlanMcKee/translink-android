package com.lach.translink.data.journey.favourite;

import com.lach.translink.data.DbFlowDao;
import com.lach.translink.data.journey.JourneySearch;
import com.lach.translink.data.journey.JourneySearchList;
import com.lach.translink.util.DataResource;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Dao in charge of delivering {@link JourneyCriteriaFavourite} database results.
 */
public class JourneyCriteriaFavouriteDao extends DbFlowDao<JourneyCriteriaFavourite, JourneyCriteriaFavouriteModel> {

    @Override
    public Class<? extends Model> getModelClass() {
        return JourneyCriteriaFavouriteModel.class;
    }

    @Override
    public Where<JourneyCriteriaFavouriteModel> getAllRowsQuery() {
        // Sort by order of insertion.
        return new Select()
                .from(JourneyCriteriaFavouriteModel.class)
                .orderBy(OrderBy.columns(JourneyCriteriaFavouriteModel$Table.DATECREATED).ascending());
    }

    @Override
    public JourneyCriteriaFavouriteModel createModel() {
        return new JourneyCriteriaFavouriteModel();
    }

    /**
     * Unfortunately the old mechanism for saving data did not use a good implementation.
     * <p/>
     * This must be safely transferred into the new database.
     *
     * @param journeysFavouritesList the previous persisted collection of journey favourites.
     */
    public void migrateOldData(JourneySearchList journeysFavouritesList) {
        TransactionManager instance = TransactionManager.getInstance();

        List<JourneyCriteriaFavouriteModel> journeyCriteriaFavouriteList = new ArrayList<>();
        Calendar dateCreatedCalendar = Calendar.getInstance();
        dateCreatedCalendar.setTime(new Date());

        //
        // Convert all the old implementations to the new one.
        //
        for (JourneySearch savedJourneySearch : journeysFavouritesList) {
            // Increment the time slightly so the ordering remains the same.
            dateCreatedCalendar.set(Calendar.MINUTE, dateCreatedCalendar.get(Calendar.MINUTE) + 1);

            JourneyCriteriaFavouriteModel journeyCriteriaFavourite = createModel();
            journeyCriteriaFavourite.setName(savedJourneySearch.journeyName);
            journeyCriteriaFavourite.setDateCreated(dateCreatedCalendar.getTime());
            journeyCriteriaFavourite.setJourneyCriteria(DataResource.convertJourneySearchToCriteria(savedJourneySearch));

            journeyCriteriaFavouriteList.add(journeyCriteriaFavourite);
        }

        // Run an asynchronous task to insert all the new values.
        instance.addTransaction(new InsertModelTransaction<>(ProcessModelInfo.withModels(journeyCriteriaFavouriteList)));
    }
}
