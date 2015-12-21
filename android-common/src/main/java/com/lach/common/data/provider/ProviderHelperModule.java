package com.lach.common.data.provider;

import dagger.Module;
import dagger.Provides;

@Module
public class ProviderHelperModule {

    @Provides
    public ContactAddressExtractor provideContactAddressExtractor() {
        return new ContactAddressExtractor();
    }

}