package com.lach.translink.data.journey.history;

import com.lach.translink.data.TranslinkDatabase;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

@Table(databaseName = TranslinkDatabase.NAME, tableName = "JourneyCriteriaHistory")
public class JourneyCriteriaHistoryModel extends BaseModel implements JourneyCriteriaHistory {

    @Column(name = "id")
    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String fromAddress;

    @Column
    public String toAddress;

    @Column
    public JourneyTransport journeyTransport;

    @Column
    public JourneyTimeCriteria journeyTimeCriteria;

    @Column
    public Date time;

    @Column
    @NotNull
    public Date dateCreated;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public JourneyCriteria getJourneyCriteria() {
        JourneyCriteria criteria = new JourneyCriteria();
        criteria.setFromAddress(fromAddress);
        criteria.setToAddress(toAddress);

        criteria.setJourneyTransport(journeyTransport);
        criteria.setJourneyTimeCriteria(journeyTimeCriteria);
        criteria.setTime(time);
        return criteria;
    }

    @Override
    public void setJourneyCriteria(JourneyCriteria criteria) {
        fromAddress = criteria.getFromAddress();
        toAddress = criteria.getToAddress();

        journeyTransport = criteria.getJourneyTransport();
        journeyTimeCriteria = criteria.getJourneyTimeCriteria();
        time = criteria.getTime();
    }

}