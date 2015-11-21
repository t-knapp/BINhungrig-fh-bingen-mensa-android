package de.fhbingen.mensa.data.event;

/**
 * Created by tknapp on 21.11.15.
 */
public class NetworkStatusEvent {

    public NetworkStatusEvent(final boolean connected){
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    private boolean connected;
}
