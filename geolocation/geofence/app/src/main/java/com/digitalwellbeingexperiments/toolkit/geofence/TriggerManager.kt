// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.digitalwellbeingexperiments.toolkit.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.launch

private const val GEO_REQUEST_CODE = 111234
private const val NOTIFICATION_RESPONSIVENESS_MS = 2000

class TriggerManager(private val context: Context) {

    interface Listener {
        fun onUpdate()
    }
    var listener: Listener? = null
    private val store = TriggerStore(context)
    private var pendingIntent: PendingIntent? = PendingIntent.getBroadcast(
        context,
        GEO_REQUEST_CODE,
        Intent(context, GeofenceBroadcastReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun getAll() = store.getAll()

    fun add(trigger: Trigger, successCallback: (result: Boolean, errorMessage: String?) -> Unit) {

        //build geofence
        val geofence = Geofence.Builder()
            .setRequestId(trigger.id)
            .setCircularRegion(
                trigger.latLng.latitude, // latitude
                trigger.latLng.longitude, // longitude
                trigger.radius  // radius (m)
            )
            .setNotificationResponsiveness(NOTIFICATION_RESPONSIVENESS_MS)
            .setLoiteringDelay(0)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        //build the request
        val geoRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(listOf(geofence))
            .build()

        LocationServices.getGeofencingClient(context)?.let { client ->
            client.addGeofences(geoRequest, pendingIntent)?.run {
                addOnSuccessListener {
                    Log.w(TAG(), "added geofence ${trigger.placeName}")
                    store.add(trigger)
                    successCallback(true, null)
                }
                addOnFailureListener {
                    var errorDetails = it.message
                    if (it is ApiException) {
                        errorDetails =
                            "ApiException status (${it.statusCode}) ${GeofenceStatusCodes.getStatusCodeString(
                                it.statusCode
                            )}"
                    }
                    Log.w(
                        TAG(),
                        "failed to add geofence ${trigger.placeName}, Exception details $errorDetails"
                    )
                    successCallback(false, errorDetails)
                }
            }
        }
    }

    fun remove(trigger: Trigger, successCallback: (result: Boolean) -> Unit) {
        LocationServices.getGeofencingClient(context)?.let { client ->
            client.removeGeofences(listOf(trigger.id))?.run {
                addOnSuccessListener {
                    store.remove(trigger)
                    Log.w(TAG(), "* geofence REMOVED ${trigger.placeName} - ${trigger.id}")
                    successCallback(true)
                }
                addOnFailureListener {
                    Log.w(TAG(), "* geofence removal FAIL ${trigger.placeName} - ${trigger.id}")
                    successCallback(false)
                }
            }
        }
    }

    fun onTrigger(requestId: String, enterTimeStamp: Long){
        val trigger = store.get(requestId)
        trigger?.let {
            it.triggerTimestamp = enterTimeStamp
            store.update(it)
            mainScope.launch {
                listener?.onUpdate()
            }
        }
    }
}