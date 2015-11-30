package com.lach.common.tasks;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.lach.common.async.AsyncResult;
import com.lach.common.async.Task;
import com.lach.common.data.TaskGenericErrorType;
import com.lach.common.log.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class TaskGetAddress implements Task<List<Address>> {
    private static final String TAG = "TaskGetAddress";
    private final WeakReference<Context> contextRef;

    @Inject
    public TaskGetAddress(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    public AsyncResult<List<Address>> execute(Object... params) {
        Context context = contextRef.get();
        if (context == null) {
            Log.warn(TAG, "Context was not set.");
            return null;
        }

        LatLng destination = (LatLng) params[0];
        try {
            Geocoder gc = new Geocoder(context, Locale.getDefault());
            List<Address> addressList = gc.getFromLocation(destination.latitude, destination.longitude, 1);

            if (addressList != null) {
                return new AsyncResult<>(addressList);
            }

        } catch (IOException ex) {
            Log.warn(TAG, "Error occurred while obtaining location", ex);
        }

        return new AsyncResult<>(TaskGenericErrorType.INVALID_NETWORK_RESPONSE);
    }

}
