package it.sellainbox.cache;

import android.content.Context;
import android.content.SharedPreferences;

import it.sellainbox.service.BeaconsMonitoringService;

/**
 * Created by PremKumar on 02/09/15.
 */
public class SellaCache {

    public static final String PREFERENCE = "SellaInbox";

    public static void putCache(String key, String value, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFERENCE, context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getCache(String key, String defaultValue, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE, context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }
}
