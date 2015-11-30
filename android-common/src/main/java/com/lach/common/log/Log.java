package com.lach.common.log;

import java.util.HashMap;

public class Log {

    public static void debug(String tag, String message) {
        android.util.Log.d(tag, message);
    }

    public static void warn(String tag, String message) {
        android.util.Log.w(tag, message);
    }

    public static void warn(String tag, String message, Exception exception) {
        android.util.Log.w(tag, message, exception);
    }

    public static void error(String tag, String message) {
        error(tag, message, new Exception(message));
    }

    public static void error(String tag, String message, Exception exception) {
        error(tag, message, exception, null);
    }

    public static void error(String tag, String message, Exception exception, String extraMessageKey, String extraMessageValue) {
        android.util.Log.e(tag, message, exception);

        HashMap<String, Object> customData = new HashMap<>();
        customData.put(extraMessageKey, extraMessageValue);

        error(tag, message, exception, customData);
    }

    public static void error(String tag, String message, Exception exception, HashMap<String, Object> customData) {
        android.util.Log.e(tag, message, exception);

        if (customData == null) {
            customData = new HashMap<>();
        }
        customData.put("tag", tag);
        customData.put("message", message);

        Instrumentation.logExceptionWithData(exception, customData);
    }

}
