package com.lach.translink.data.place;

import android.database.sqlite.SQLiteDatabase;

import com.lach.translink.data.TranslinkDatabase;
import com.lach.translink.data.place.bus.BusStopModel$Adapter;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

@Migration(databaseName = TranslinkDatabase.NAME, version = 2)
public class BusStopMigration extends BaseMigration {
    @Override
    public void migrate(SQLiteDatabase database) {
        BusStopModel$Adapter adapter = new BusStopModel$Adapter();
        database.execSQL(adapter.getCreationQuery());
    }
}