package de.fhbingen.mensa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import de.fhbingen.mensa.data.event.NetworkStatusEvent;
import de.greenrobot.event.EventBus;

/**
 * Receiver of Network Change State Events
 * Forewards the state via Eventbus to other components
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    private final static String TAG = NetworkStateReceiver.class.getSimpleName();

    public NetworkStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        if(intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(ni!=null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected");

                // Post status
                EventBus.getDefault().post(new NetworkStatusEvent(true));
            } else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity");

                // Post status
                EventBus.getDefault().post(new NetworkStatusEvent(false));
            }
        }
    }
}
