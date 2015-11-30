package com.lach.translink.data.journey.history;

import com.lach.translink.data.DbFlowDao;
import com.lach.translink.data.journey.JourneySearch;
import com.lach.translink.data.journey.JourneySearchList;
import com.lach.translink.util.DataResource;
import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.DeleteTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.InsertModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Dao in charge of delivering {@link JourneyCriteriaHistoryDao} database results.
 */
public class JourneyCriteriaHistoryDao extends DbFlowDao<JourneyCriteriaHistory, JourneyCriteriaHistoryModel> {

    public static final int MAX_ROWS = 3;

    @Override
    public Class<? extends Model> getModelClass() {
        return JourneyCriteriaHistoryModel.class;
    }

    @Override
    public Where<JourneyCriteriaHistoryModel> getAllRowsQuery() {
        return new Select()
                .from(JourneyCriteriaHistoryModel.class)
                .orderBy(OrderBy.columns(JourneyCriteriaHistoryModel$Table.DATECREATED).descending());
    }

    @Override
    public JourneyCriteriaHistoryModel createModel() {
        return new JourneyCriteriaHistoryModel();
    }

    @Override
    public void insertRows(List<JourneyCriteriaHistory> itemsToAdd) {
        TransactionManager instance = TransactionManager.getInstance();

        //
        // Check if we need to remove any old records, add one to account for the new one being added.
        //
        int newRowCount = (int) getRowCount() + itemsToAdd.size();
        if (newRowCount > MAX_ROWS) {
            Where<JourneyCriteriaHistoryModel> oldHistorySubQuery = new Select(JourneyCriteriaHistoryModel$Table.ID)
                    .from(JourneyCriteriaHistoryModel.class)
                    .orderBy(OrderBy.columns(JourneyCriteriaHistoryModel$Table.DATECREATED).ascending())
                    .limit(newRowCount - MAX_ROWS);

            instance.addTransaction(new DeleteTransaction<>(
                    DBTransactionInfo.create(),
                    new ConditionQueryBuilder<>(JourneyCriteriaHistoryModel.class,
                            Condition.column(JourneyCriteriaHistoryModel$Table.ID).in(oldHistorySubQuery))
            ));
        }

        super.insertRows(itemsToAdd);
    }

    /**
     * Unfortunately the old mechanism for saving data did not use a good implementation.
     * <p/>
     * This must be safely transferred into the new database.
     *
     * @param journeysHistoryList the previous persisted collection of journey history.
     */
    public void migrateOldData(JourneySearchList journeysHistoryList) {
        TransactionManager instance = TransactionManager.getInstance();

        List<JourneyCriteriaHistoryModel> journeyCriteriaHistoryList = new ArrayList<>();
        Calendar dateCreatedCalendar = Calendar.getInstance();
        dateCreatedCalendar.setTime(new Date());

        // Go back slightly to ensure that history remains in the past.
        dateCreatedCalendar.set(Calendar.DAY_OF_MONTH, dateCreatedCalendar.get(Calendar.DAY_OF_MONTH) + 1);

        //
        // Convert all the old implementations to the new one.
        //
        for (JourneySearch journeySearchHistory : journeysHistoryList) {
            // Increment the time slightly so the ordering remains the same.
            dateCreatedCalendar.set(Calendar.MINUTE, dateCreatedCalendar.get(Calendar.MINUTE) + 1);

            JourneyCriteriaHistoryModel journeyCriteriaHistory = createModel();
            journeyCriteriaHistory.setDateCreated(dateCreatedCalendar.getTime());
            journeyCriteriaHistory.setJourneyCriteria(DataResource.convertJourneySearchToCriteria(journeySearchHistory));

            journeyCriteriaHistoryList.add(journeyCriteriaHistory);
        }

        // Run an asynchronous task to insert all the new values.
        instance.addTransaction(new InsertModelTransaction<>(ProcessModelInfo.withModels(journeyCriteriaHistoryList)));
    }
}
