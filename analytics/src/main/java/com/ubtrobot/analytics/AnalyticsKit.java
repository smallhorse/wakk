package com.ubtrobot.analytics;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;

import com.ubtrobot.analytics.ipc.AnalyticsConstants;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsKit {

    private static volatile Analytics sAnalytics;

    private AnalyticsKit() {
    }

    public static void initialize(Context context) {
        if (sAnalytics != null) {
            return;
        }

        synchronized (AnalyticsKit.class) {
            if (sAnalytics != null) {
                return;
            }

            ContentResolver resolver = context.getContentResolver();
            sAnalytics = new ProviderAnalyticsProxy(resolver);
        }
    }

    private static void checkAnalytics() {
        if (sAnalytics == null) {
            throw new IllegalStateException(
                    "Please call com.ubtrobot.analytics.AnalyticsKit.initialize");
        }
    }

    public static Strategy getStrategy() {
        checkAnalytics();
        return sAnalytics.getStrategy();
    }

    public static void recordEvent(String eventId) {
        recordEvent(eventId, null);
    }

    public static void recordEvent(String eventId, long duration) {
        recordEvent(eventId, duration, null);
    }

    public static void recordEvent(
            String eventId, long duration, Map<String, String> segmentation) {
        if (eventId == null || eventId.length() <= 0 ||
                eventId.length() > AnalyticsConstants.ID_MAX_LENGTH) {
            throw new IllegalArgumentException("Argument need 0 < eventId.length <= 64.");
        }
        if (duration < 0) {
            throw new IllegalArgumentException("Argument duration < 0.");
        }

        checkAnalytics();

        Event.Builder builder = new Event.Builder(
                eventId, AnalyticsConstants.EVENT_CATEGORY_CUSTOM).setDuration(duration);
        builder.setCustomSegmentation(segmentation);

        recordEvent(builder.build());
    }

    private static void recordEvent(Event event) {
        sAnalytics.recordEvent(event);
    }

    public static void recordEvent(String eventId, Map<String, String> segmentation) {
        recordEvent(eventId, 0, segmentation);
    }

    public static void recordActivityStart(Activity activity) {
        recordActivityStart(activity.getClass().getName());
    }

    public static void recordActivityStart(String activityName) {
        checkString("activityName", activityName);
        recordEvent(new Event.Builder(AnalyticsConstants.EVENT_ID_PAGE_START,
                AnalyticsConstants.EVENT_CATEGORY_PAGE).
                setSegmentation(
                        createSegmentation(activityName, AnalyticsConstants.EVENT_TYPE_ACTIVITY)).
                build());
    }

    private static void checkString(String stringName, String stringValue) {
        if (stringValue == null || stringValue.length() <= 0) {
            throw new IllegalArgumentException(
                    "Argument:" + stringName + " is null.");
        }
    }

    public static void recordFragmentStart(String fragmentName) {
        checkString("fragmentName", fragmentName);
        recordEvent(new Event.Builder(AnalyticsConstants.EVENT_ID_PAGE_START,
                AnalyticsConstants.EVENT_CATEGORY_PAGE).
                setSegmentation(
                        createSegmentation(fragmentName, AnalyticsConstants.EVENT_TYPE_FRAGMENT)).
                build());
    }

    public static void recordActivityStop(Activity activity) {
        recordActivityStop(activity.getClass().getName());
    }

    public static void recordActivityStop(String activityName) {
        checkString("activityName", activityName);
        recordEvent(new Event.Builder(AnalyticsConstants.EVENT_ID_PAGE_END,
                AnalyticsConstants.EVENT_CATEGORY_PAGE).
                setSegmentation(
                        createSegmentation(activityName, AnalyticsConstants.EVENT_TYPE_ACTIVITY)).
                build());
    }

    public static void recordFragmentStop(String fragmentName) {
        checkString("fragmentName", fragmentName);
        recordEvent(new Event.Builder(AnalyticsConstants.EVENT_ID_PAGE_END,
                AnalyticsConstants.EVENT_CATEGORY_PAGE).
                setSegmentation(
                        createSegmentation(fragmentName, AnalyticsConstants.EVENT_TYPE_FRAGMENT)).
                build());
    }

    private static Map<String, String> createSegmentation(String pageName, String pageType) {
        Map<String, String> segmentation = new HashMap<>();
        segmentation.put("type", pageType);
        segmentation.put("name", pageName);
        return segmentation;
    }

}
