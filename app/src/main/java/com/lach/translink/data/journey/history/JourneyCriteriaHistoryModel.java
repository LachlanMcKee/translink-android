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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public String journeyTransport;

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

        List<JourneyTransport> transportList = null;

        if (journeyTransport != null) {
            String[] typeList = journeyTransport.split(",");
            if (typeList.length > 0) {
                transportList = new ArrayList<>();

                for (String type : typeList) {
                    transportList.add(JourneyTransport.valueOf(type));
                }

            }
        }

        criteria.setJourneyTransport(transportList);
        criteria.setJourneyTimeCriteria(journeyTimeCriteria);
        criteria.setTime(time);
        return criteria;
    }

    @Override
    public void setJourneyCriteria(JourneyCriteria criteria) {
        fromAddress = criteria.getFromAddress();
        toAddress = criteria.getToAddress();

        String transport = null;
        List<JourneyTransport> transportList = criteria.getJourneyTransport();
        if (transportList != null) {
            if (transportList.size() > 0) {
                StringBuilder transportBuilder = new StringBuilder();

                for (JourneyTransport jt : transportList) {
                    transportBuilder.append(jt.toString());
                }
                transport = transportBuilder.toString();
            }

        }
        this.journeyTransport = transport;
        journeyTimeCriteria = criteria.getJourneyTimeCriteria();
        time = criteria.getTime();
    }

}