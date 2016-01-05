package de.fhbingen.mensa.data.event;

/**
 * Created by tknapp on 05.01.16.
 */
public class BuildingChangedEvent {
    public int selectedBuildingId;

    public BuildingChangedEvent(int selectedBuildingId){
        this.selectedBuildingId = selectedBuildingId;
    }
}
