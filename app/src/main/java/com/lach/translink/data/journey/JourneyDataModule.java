package com.lach.translink.data.journey;

import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;

import dagger.Module;
import dagger.Provides;

@Module
public class JourneyDataModule {

    @Provides
    public JourneyCriteriaFavouriteDao provideJourneyCriteriaFavouriteDao() {
        return new JourneyCriteriaFavouriteDao();
    }

    @Provides
    public JourneyCriteriaHistoryDao provideJourneyCriteriaHistoryDao() {
        return new JourneyCriteriaHistoryDao();
    }

}
