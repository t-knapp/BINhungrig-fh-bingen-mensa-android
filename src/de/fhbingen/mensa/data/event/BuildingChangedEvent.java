package de.fhbingen.mensa.data.event;

/**
 * EventBus Event fired when user changed selected building in MainActivity
 *
 * Created by tknapp on 05.01.16.
 */
public class BuildingChangedEvent {
    public int selectedBuildingId;

    public BuildingChangedEvent(int selectedBuildingId){
        this.selectedBuildingId = selectedBuildingId;
    }
}
