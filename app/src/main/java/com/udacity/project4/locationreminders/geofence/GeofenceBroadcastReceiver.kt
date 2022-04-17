package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
private const val TAG = "GeofenceReceiver"

class GeofenceBroadcastReceiver : BroadcastReceiver() {
//TODO: implement the onReceive method to receive the geofencing events at the background

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                Toast.makeText(
                    context, "geofencingEvent error",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e(TAG, "geofencingEvent error")
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))
                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }
                Toast.makeText(
                    context, "geofencingEvent enter $fenceId",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e(TAG, "geofencingEvent enter")
            }else if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Toast.makeText(
                    context, "geofencingEvent exit",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }else if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Toast.makeText(
                    context, "geofencingEvent dwell",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }
}