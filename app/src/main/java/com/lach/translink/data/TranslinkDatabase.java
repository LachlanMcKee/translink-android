package com.lach.translink.data;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(
        name = TranslinkDatabase.NAME,
        version = TranslinkDatabase.VERSION,
        foreignKeysSupported = true
)
public class TranslinkDatabase {
    public static final String NAME = "Translink";

    public static final int VERSION = 1;
}