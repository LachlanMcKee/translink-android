package com.lach.translink.data;

import android.database.sqlite.SQLiteDatabase;

import com.lach.translink.data.place.BusStopModel$Adapter;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

@Migration(databaseName = TranslinkDatabase.NAME, version = 2)
public class TranslinkMigration extends BaseMigration {
    @Override
    public void migrate(SQLiteDatabase database) {
        BusStopModel$Adapter adapter = new BusStopModel$Adapter();
        database.execSQL(adapter.getCreationQuery());
    }
}