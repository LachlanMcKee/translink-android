package com.lach.translink.data.place;

import com.lach.translink.data.DbFlowDao;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

public class BusStopDao extends DbFlowDao<BusStop, BusStopModel> {
    @Override
    public Class<? extends Model> getModelClass() {
        return BusStopModel.class;
    }

    @Override
    public Where<BusStopModel> getAllRowsQuery() {
        return new Select()
                .from(BusStopModel.class)
                .where();
    }

    @Override
    public BusStopModel createModel() {
        return new BusStopModel();
    }
}
