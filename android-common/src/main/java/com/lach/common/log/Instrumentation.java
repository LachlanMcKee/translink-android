package com.lach.common.log;

import android.content.Context;

import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import java.util.HashMap;

public class Instrumentation {
    private static final boolean ACTIVE = true;

    public static void init(Context context, String apiKey) {
        if (!ACTIVE) {
            return;
        }
        Mint.initAndStartSession(context, apiKey);
    }

    public static void logException(Exception exception) {
        if (!ACTIVE) {
            return;
        }
        HashMap<String, Object> extraData = new HashMap<>(0);
        logExceptionWithData(exception, extraData);
    }

    public static void logExceptionWithData(Exception exception, HashMap<String, Object> customData) {
        if (!ACTIVE) {
            return;
        }
        Mint.logExceptionMap(customData, exception);
    }

    public static void logEvent(String eventName) {
        if (!ACTIVE) {
            return;
        }
        Mint.logEvent(eventName);
    }

    public static void logEvent(String eventName, InstrumentationLogLevel logLevel) {
        if (!ACTIVE) {
            return;
        }
        Mint.logEvent(eventName, getMintLogLevel(logLevel));
    }

    public static void logEventWithData(String eventName, InstrumentationLogLevel logLevel, String keyName, String keyValue) {
        if (!ACTIVE) {
            return;
        }
        Mint.logEvent(eventName, getMintLogLevel(logLevel), keyName, keyValue);
    }

    public static void logEventWithData(String eventName, InstrumentationLogLevel logLevel, HashMap<String, Object> customData) {
        Mint.logEvent(eventName, getMintLogLevel(logLevel), customData);
    }

    private static MintLogLevel getMintLogLevel(InstrumentationLogLevel logLevel) {
        switch (logLevel) {
            case Verbose:
                return MintLogLevel.Verbose;

            case Debug:
                return MintLogLevel.Debug;

            case Info:
                return MintLogLevel.Info;

            case Warning:
                return MintLogLevel.Warning;

            case Error:
            default:
                return MintLogLevel.Error;
        }
    }

}
