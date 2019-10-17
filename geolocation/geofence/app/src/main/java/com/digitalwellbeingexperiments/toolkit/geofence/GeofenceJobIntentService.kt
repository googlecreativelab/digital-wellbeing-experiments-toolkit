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

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceJobIntentService: JobIntentService() {
    companion object {
        private const val JOB_ID = 100
        private const val ENTER_TIMESTAMP = "timestamp"

        fun addJob(context: Context, intent: Intent) {
            Log.w(TAG(),  "addJob()")
            intent.putExtra(ENTER_TIMESTAMP, System.currentTimeMillis())
            enqueueWork(
                context,
                GeofenceJobIntentService::class.java,
                JOB_ID,
                intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        Log.w(TAG(), "onHandleWork()")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        //handle errors
        if (geofencingEvent.hasError()) {
            Log.e(TAG(),  "geofence error: ${geofencingEvent.errorCode}")
            return
        }

        val geofence = geofencingEvent.triggeringGeofences[0]
        when(geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.w(TAG(),
                    "GEOFENCE TRANSITION ENTER TRIGGER DETECTED for req ID: ${geofence.requestId}"
                )
                GeofenceApplication.instance.triggerManager.onTrigger(geofence.requestId,
                    intent.getLongExtra(ENTER_TIMESTAMP, System.currentTimeMillis()))
            }
        }
    }
}