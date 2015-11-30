package com.lach.translink.activities.data;

import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockJourneyDataModule {

    @Singleton
    @Provides
    JourneyCriteriaFavouriteDao provideJourneyCriteriaFavouriteDao() {
        return Mockito.mock(JourneyCriteriaFavouriteDao.class);
    }

    @Singleton
    @Provides
    JourneyCriteriaHistoryDao provideJourneyCriteriaHistoryDao() {
        return Mockito.mock(JourneyCriteriaHistoryDao.class);
    }

}
