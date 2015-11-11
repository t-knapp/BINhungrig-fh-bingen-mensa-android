package de.fhbingen.mensa;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fhbingen.mensa.data.orm.Building;

/**
 * Created by tknapp on 11.11.15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Set Values for buildings
        setupChooseBuildingControl("pref_key_subscribed_buildings");
    }

    private void setupChooseBuildingControl(final String preferenceKey){
        MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) findPreference(preferenceKey);

        List<Building> buildings = Building.findAll();

        CharSequence[] arBuildingEntries = new CharSequence[buildings.size()];
        CharSequence[] arBuildingValues  = new CharSequence[buildings.size()];

        Building currentBuilding;
        for(int i = 0; i < buildings.size(); i++){
            currentBuilding = buildings.get(i);
            arBuildingEntries[i] = currentBuilding.getName();
            arBuildingValues[i]  = Integer.toString(currentBuilding.getBuildingId());
        }

        multiSelectListPreference.setEntries(arBuildingEntries);
        multiSelectListPreference.setEntryValues(arBuildingValues);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if(key.equals("pref_key_user_role")){
            pref.setSummary(((ListPreference) pref).getEntry());
        } else if (key.equals("pref_key_subscribed_buildings")){
            final Set<String> values = ((MultiSelectListPreference) pref).getValues();

            pref.setSummary(buildingIdsToSummary(values));
        }
    }

    private String buildingIdsToSummary(final Set<String> values){
        final List<String> buildingNames = new ArrayList<String>();
        Building b;
        for(String value : values){
            b = Building.findByBuildingId(value);
            if(b != null)
                buildingNames.add(b.getName());
        }
        return TextUtils.join(", ", buildingNames.toArray());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Set Summary for Role
        Preference preference = findPreference("pref_key_user_role");
        int index = Integer.parseInt(getPreferenceManager().getSharedPreferences().getString("pref_key_user_role", "0"));
        Resources res = getResources();
        String value = res.getStringArray(R.array.settings_items_user_role)[index];
        preference.setSummary(value);

        // Set Summary for Subscribed Buildings
        preference = findPreference("pref_key_subscribed_buildings");
        final Set<String> values = getPreferenceManager().getSharedPreferences().getStringSet("pref_key_subscribed_buildings", null);

        preference.setSummary(buildingIdsToSummary(values));
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
