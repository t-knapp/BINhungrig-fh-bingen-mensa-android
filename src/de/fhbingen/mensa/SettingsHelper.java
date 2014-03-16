package de.fhbingen.mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Maxi on 16.03.14.
 */
public class SettingsHelper {
    public static boolean isAutoDownloadLargePicturesEnabled() {
        return settings.getBoolean("isAutoDownloadLargePicturesEnabled", true);
    }

    public static void setIsAutoDownloadLargePicturesEnabled(boolean isAutoDownloadLargePicturesEnabled) {
        editor.putBoolean("isAutoDownloadLargePicturesEnabled", isAutoDownloadLargePicturesEnabled);
        editor.apply();
    }

    private static SharedPreferences settings = Mensa.getMensa().getApplicationContext().getSharedPreferences(Mensa.PREF_USER, Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = settings.edit();


    private SettingsHelper() {}
}
