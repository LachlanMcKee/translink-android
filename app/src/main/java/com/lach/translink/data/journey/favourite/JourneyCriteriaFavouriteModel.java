package com.lach.translink.data.journey.favourite;

import com.lach.translink.data.TranslinkDatabase;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryModel;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

@Table(databaseName = TranslinkDatabase.NAME, tableName = "JourneyCriteriaFavourite")
public class JourneyCriteriaFavouriteModel extends JourneyCriteriaHistoryModel implements JourneyCriteriaFavourite {

    @Column
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}