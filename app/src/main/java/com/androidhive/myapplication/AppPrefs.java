package com.androidhive.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPrefs {

    private static final String PREFS = "app_prefs";

    private static final String KEY_LAST_LAT = "last_lat";
    private static final String KEY_LAST_LON = "last_lon";

    private static final String KEY_NEXT_PRAYER_TIME = "next_prayer_time";
    private static final String KEY_NEXT_PRAYER_NAME = "next_prayer_name";

    private static final String KEY_NOTIF_HIDDEN = "notif_hidden";
    private static final String KEY_AUTO_ADHAN = "auto_adhan";

    private static final String KEY_LOCATION_MODE = "location_mode"; // none/gps/manual
    private static final String KEY_LOCATION_CITY = "location_city"; // فقط برای manual

    private static final String KEY_PRAYER_NOTIF_ENABLED = "prayer_notif_enabled"; // نوتیفیکیشن وقت اذان


    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---- LOCATION ----
    public static void saveLocation(Context c, double lat, double lon) {
        p(c).edit()
                .putLong(KEY_LAST_LAT, Double.doubleToRawLongBits(lat))
                .putLong(KEY_LAST_LON, Double.doubleToRawLongBits(lon))
                .apply();
    }


    public static double getLat(Context c) {
        SharedPreferences prefs = p(c);
        try {
            long v = prefs.getLong(KEY_LAST_LAT, 0L);
            return v == 0L ? 0.0 : Double.longBitsToDouble(v);
        } catch (ClassCastException e) {
            float old = prefs.getFloat(KEY_LAST_LAT, 0f);
            double val = (double) old;
            prefs.edit().putLong(KEY_LAST_LAT, Double.doubleToRawLongBits(val)).apply();
            return val;
        }
    }

    public static double getLon(Context c) {
        SharedPreferences prefs = p(c);
        try {
            long v = prefs.getLong(KEY_LAST_LON, 0L);
            return v == 0L ? 0.0 : Double.longBitsToDouble(v);
        } catch (ClassCastException e) {
            float old = prefs.getFloat(KEY_LAST_LON, 0f);
            double val = (double) old;
            prefs.edit().putLong(KEY_LAST_LON, Double.doubleToRawLongBits(val)).apply();
            return val;
        }
    }



    public static void clearAll(Context c) {
        p(c).edit().clear().apply();
    }


    // ---- NEXT PRAYER ----
    public static void saveNextPrayer(Context c, String nameFa, long timeMillis) {
        p(c).edit()
                .putString(KEY_NEXT_PRAYER_NAME, nameFa)
                .putLong(KEY_NEXT_PRAYER_TIME, timeMillis)
                .apply();
    }

    public static String getNextPrayerName(Context c) {
        return p(c).getString(KEY_NEXT_PRAYER_NAME, "");
    }

    public static long getNextPrayerTime(Context c) {
        return p(c).getLong(KEY_NEXT_PRAYER_TIME, 0L);
    }

    // اگر جایی از پروژه هنوز getNextPrayerTimeMillis صدا می‌زنه، اینو نگه می‌داریم
    public static long getNextPrayerTimeMillis(Context c) {
        return getNextPrayerTime(c);
    }

    // ---- NOTIFICATION STATE ----
    public static void setNotifHidden(Context c, boolean hidden) {
        p(c).edit().putBoolean(KEY_NOTIF_HIDDEN, hidden).apply();
    }

    public static boolean isNotifHidden(Context c) {
        return p(c).getBoolean(KEY_NOTIF_HIDDEN, false);
    }

    public static void resetNotifHiddenOnNewPrayer(Context c) {
        setNotifHidden(c, false);
    }

    // ---- AUTO ADHAN ----
    public static void setAutoAdhanEnabled(Context c, boolean enabled) {
        p(c).edit().putBoolean(KEY_AUTO_ADHAN, enabled).apply();
    }

    public static boolean isAutoAdhanEnabled(Context c) {
        return p(c).getBoolean(KEY_AUTO_ADHAN, false);
    }

    // ---- LOCATION MODE / LABEL ----
    public static void setLocationMode(Context c, String mode) {
        p(c).edit().putString(KEY_LOCATION_MODE, mode).apply();
    }

    public static String getLocationMode(Context c) {
        return p(c).getString(KEY_LOCATION_MODE, "none");
    }

    public static void setManualCityName(Context c, String city) {
        p(c).edit().putString(KEY_LOCATION_CITY, city == null ? "" : city).apply();
    }

    public static String getManualCityName(Context c) {
        return p(c).getString(KEY_LOCATION_CITY, "");
    }

    // ---- PRAYER NOTIFICATIONS ----
    public static void setPrayerNotificationsEnabled(Context c, boolean enabled) {
        p(c).edit().putBoolean(KEY_PRAYER_NOTIF_ENABLED, enabled).apply();
    }

    public static boolean isPrayerNotificationsEnabled(Context c) {
        return p(c).getBoolean(KEY_PRAYER_NOTIF_ENABLED, true);
    }

}
