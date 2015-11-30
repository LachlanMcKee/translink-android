package com.lach.translink.ui.settings;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.BaseDao;
import com.lach.translink.data.location.favourite.LocationFavourite;
import com.lach.translink.data.location.favourite.LocationFavouriteDao;
import com.lach.translink.ui.resolve.ResolveLocationActivity;

import javax.inject.Inject;

public class FavouriteLocationsActivity extends CheckableListActivity<LocationFavourite> {

    private static final int RESOLVE_LOCATION_REQUEST = 1;

    @Inject
    LocationFavouriteDao locationFavouriteDao;

    @Override
    protected BaseDao<LocationFavourite, ? extends LocationFavourite> getListDao() {
        TranslinkApplication application = (TranslinkApplication) getApplication();
        application.getDataComponent().inject(this);
        return locationFavouriteDao;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESOLVE_LOCATION_REQUEST) {

            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra(ResolveLocationActivity.ADDRESS_KEY);

                LocationFavourite locationFavourite = getListDao().createModel();
                locationFavourite.setAddress(address);

                getListDao().insertRows(locationFavourite);
                reloadData();

                Toast.makeText(this, "Location added", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public String getTypeDescription() {
        return "locations";
    }

    @Override
    protected String getCheckboxText(LocationFavourite item) {
        return item.getAddress();
    }

    @Override
    protected int getRowLayoutId() {
        return R.layout.l_saved_journey;
    }

    @Override
    protected View.OnClickListener getAddClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ResolveLocationActivity.createIntent(FavouriteLocationsActivity.this), RESOLVE_LOCATION_REQUEST);
            }
        };
    }

}
