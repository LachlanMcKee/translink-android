package com.lach.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static boolean isOnline(Context context) {
        return isOnline(context, true);
    }

    public static boolean isOnline(Context context, boolean showDialog) {
        boolean isConnected;
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            isConnected = info.isConnected();
        } catch (Exception e) {

            isConnected = false;
        }
        if (!isConnected && showDialog) {
            DialogUtil.showAlertDialog(context, "You are not connected to the Internet!", "No Internet connection");
        }

        return isConnected;
    }
}
