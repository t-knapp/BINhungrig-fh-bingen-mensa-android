package de.fhbingen.mensa.data.event;

/**
 * EventBus Event fired when UpdateService has finished pull replication
 *
 * Created by tknapp on 10.11.15.
 */
public class ServiceEvent {

    private EventType type;

    private String message;

    public ServiceEvent(EventType type, String message){
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        ALLREADYUPTODATE, UPDATEDONE
    }
}
