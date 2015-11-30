package com.lach.translink.activities.data;

import com.lach.common.data.provider.ContactAddressExtractor;

import org.mockito.Mockito;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MockProviderHelperModule {

    @Singleton
    @Provides
    ContactAddressExtractor provideContactAddressExtractor() {
        return Mockito.mock(ContactAddressExtractor.class);
    }

}