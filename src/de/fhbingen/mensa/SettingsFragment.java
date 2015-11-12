package de.fhbingen.mensa;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.fhbingen.mensa.data.event.SettingsChangeEvent;
import de.fhbingen.mensa.data.orm.Building;
import de.greenrobot.event.EventBus;

/**
 * Created by tknapp on 11.11.15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    // Key for storing selected user role
    public final static String REF_KEY_USERROLE = "pref_key_user_role";

    // Key for storing a list of subscribed buildingIds
    public final static String REF_KEY_BUILDINGS = "pref_key_subscribed_buildings";

    // Key for storing the current selected building in MainActivity
    public final static String REF_KEY_CURRENT_BUILDINGID = "pref_key_current_building";

    // Dummy Int value for not used
    public final static int DUMMY_INT_NOT_USED = 1703;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // Set Values for buildings
        setupChooseBuildingControl(REF_KEY_BUILDINGS);
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
        final Preference pref = findPreference(key);

        if(key.equals(REF_KEY_USERROLE)){
            pref.setSummary(((ListPreference) pref).getEntry());
        } else if (key.equals(REF_KEY_BUILDINGS)){
            final Set<String> values = ((MultiSelectListPreference) pref).getValues();
            pref.setSummary(buildingIdsToSummary(values));
        }

        // Notify other components
        EventBus.getDefault().post(new SettingsChangeEvent(key));
    }

    private String buildingIdsToSummary(final Set<String> values){
        final List<String> buildingNames = new ArrayList<String>();
        for(String value : values){
            buildingNames.add(Building.findByBuildingId(value).getName());
        }
        return TextUtils.join(", ", buildingNames.toArray());
    }

    @Override
    public void onResume() {
        super.onResume();

        // Unregister Preferences onChange
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Set Summary for Role
        Preference preference = findPreference(REF_KEY_USERROLE);
        int index = Integer.parseInt(getPreferenceManager().getSharedPreferences().getString(REF_KEY_USERROLE, "0"));
        Resources res = getResources();
        String value = res.getStringArray(R.array.settings_items_user_role)[index];
        preference.setSummary(value);

        // Set Summary for Subscribed Buildings
        preference = findPreference(REF_KEY_BUILDINGS);
        final Set<String> values = getPreferenceManager().getSharedPreferences().getStringSet(REF_KEY_BUILDINGS, null);
        preference.setSummary(buildingIdsToSummary(values));
    }

    @Override
    public void onPause() {

        // Unregister Preferences onChange
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

}
