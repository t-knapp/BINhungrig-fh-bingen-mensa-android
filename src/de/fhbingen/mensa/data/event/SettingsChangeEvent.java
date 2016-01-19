package de.fhbingen.mensa.data.event;

/**
 * EventBus Event fired when user changes settings in SettingsActivity
 *
 * Created by tknapp on 12.11.15.
 */
public class SettingsChangeEvent {

    private String changePreference;

    public String getChangePreference() {
        return changePreference;
    }

    public SettingsChangeEvent(final String changePreference) {
        this.changePreference = changePreference;
    }
}
