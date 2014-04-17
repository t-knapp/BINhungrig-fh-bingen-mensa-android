package de.fhbingen.mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class SettingsHelper {
    public static boolean isAutoDownloadLargePicturesEnabled() {
        return settings.getBoolean("isAutoDownloadLargePicturesEnabled", true);
    }

    public static void setIsAutoDownloadLargePicturesEnabled(boolean isAutoDownloadLargePicturesEnabled) {
        editor.putBoolean("isAutoDownloadLargePicturesEnabled", isAutoDownloadLargePicturesEnabled);
        editor.apply();
    }

    public static boolean hasCamera() {
        return applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private static Context applicationContext = Mensa.getMensa().getApplicationContext();
    private static SharedPreferences settings = applicationContext.getSharedPreferences(Mensa.PREF_USER, Context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = settings.edit();


    private SettingsHelper() {}
}
