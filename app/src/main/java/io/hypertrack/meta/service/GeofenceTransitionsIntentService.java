package io.hypertrack.meta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.transmitter.model.HTTrip;
import io.hypertrack.lib.transmitter.model.callback.HTTripStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.util.GeofenceErrorMessages;
import io.hypertrack.meta.R;
import io.hypertrack.meta.util.Constants;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    private HTTransmitterService transmitterService;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceErrorMessages.getErrorString(this,
                        geofencingEvent.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            // Test that the reported transition was of interest.

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, "User entered Geo Fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.v(TAG, "User exited Geo fence");
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Log.v(TAG, "User is dwelling in geo fence.");
                sendMessage();

                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.TRIP_STATUS, false);
                editor.commit();

            } else {
                // Log the error.
                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                        geofenceTransition));
            }

        }
    }

    private void sendMessage() {

        SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String tripId = sharedpreferences.getString(Constants.TRIP_ID, "None");

        if (tripId.equalsIgnoreCase("None"))
            return;

        if (transmitterService == null) {
            HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, this);
            HyperTrack.setLogLevel(Log.VERBOSE);
            transmitterService = HTTransmitterService.getInstance(this);
        }

        transmitterService.endTrip(new HTTripStatusCallback() {
            @Override
            public void onError(Exception e) {
                Log.v(TAG, "Error while Ending trip" + e.getMessage());
            }

            @Override
            public void onSuccess(HTTrip tripDetails) {
                Log.v(TAG, "Trip Ended. Broadcasting intent.");

                Intent intent = new Intent("trip_ended");
                intent.putExtra("end_trip", true);
                LocalBroadcastManager.getInstance(GeofenceTransitionsIntentService.this).sendBroadcast(intent);
            }
        });
    }

}