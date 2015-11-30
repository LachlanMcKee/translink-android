package com.lach.common.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentUtil {

    public static void openUrl(final Context context, final String url) {
        Intent urlIntent = new Intent(Intent.ACTION_VIEW);
        urlIntent.setData(Uri.parse(url));
        context.startActivity(urlIntent);
    }

}