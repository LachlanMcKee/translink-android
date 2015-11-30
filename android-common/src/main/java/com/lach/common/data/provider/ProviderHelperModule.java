package com.lach.common.data.provider;

import dagger.Module;
import dagger.Provides;

@Module
public class ProviderHelperModule {

    @Provides
    ContactAddressExtractor provideContactAddressExtractor() {
        return new ContactAddressExtractor();
    }

}