package com.lach.translink.data.place.bus;

import com.lach.common.data.map.MapBounds;
import com.lach.translink.data.DbFlowDao;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

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

    public List<? extends BusStop> getBusStopsWithinRegion(MapBounds bounds) {
        return new Select()
                .from(BusStopModel.class)
                .where(
                        Condition.column(BusStopModel$Table.LATITUDE).lessThan(bounds.northEast.latitude),
                        Condition.column(BusStopModel$Table.LONGITUDE).lessThan(bounds.northEast.longitude),
                        Condition.column(BusStopModel$Table.LATITUDE).greaterThan(bounds.southWest.latitude),
                        Condition.column(BusStopModel$Table.LONGITUDE).greaterThan(bounds.southWest.longitude)
                )
                .orderBy(OrderBy.columns(BusStopModel$Table.ID).ascending())
                .queryList();
    }
}
