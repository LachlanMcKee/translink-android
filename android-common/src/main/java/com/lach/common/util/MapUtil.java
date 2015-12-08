package com.lach.common.util;

import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.MapView;
import com.lach.common.log.Log;

public class MapUtil {
    private static final String TAG = MapUtil.class.getSimpleName();

    public static void moveLocationButtonToBottomRight(MapView mapView, int buttonMargin) {
        try {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent())
                    .findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, buttonMargin, buttonMargin);

        } catch (Exception ignored) {
            // If we fail to move the button it isn't the end of the world.
            Log.warn(TAG, "Unable to move the myLocation button.");
        }
    }

}
