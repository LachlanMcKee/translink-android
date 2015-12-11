package com.lach.common.log;

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
        android.util.Log.e(tag, message, exception);
        Instrumentation.logException(exception);
    }

}
