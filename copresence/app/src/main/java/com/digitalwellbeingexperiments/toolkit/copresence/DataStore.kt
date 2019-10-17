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

package com.digitalwellbeingexperiments.toolkit.copresence

import android.content.Context
import android.os.Build
import java.util.*

class DataStore(context: Context) {
    companion object {
        private const val PREF_NAME = "datastore"
        private const val KEY_NAME = "name"
        private const val KEY_DEVICE_ID = "device_id"
    }

    interface Listener {
        fun onSessionListUpdated()
    }

    var listener: Listener? = null

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val sessions = HashMap<String, NearbyDeviceSession>()

    fun getLocalDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun getName(): String {
        val defaultName = "${Build.MANUFACTURER} ${Build.MODEL} [${getLocalDeviceId().take(8)}]"
        return prefs.getString(KEY_NAME, defaultName) ?: defaultName
    }

    fun addSession(session: NearbyDeviceSession) {
        sessions[session.deviceId] = session
        listener?.onSessionListUpdated()
    }

    fun removeSession(deviceId: String) {
        sessions.remove(deviceId)
        listener?.onSessionListUpdated()
    }

    fun sessionExists(deviceId: String) = sessions.keys.contains(deviceId)

    fun clearExpiredSessions() {
        val sessionsToRemove = sessions.filter { it.value.hasExpired() }.keys
        sessionsToRemove.forEach { deviceId ->
            sessions.remove(deviceId)
        }

        if (sessionsToRemove.isNotEmpty()) {
            listener?.onSessionListUpdated()
        }
    }

    fun getAllSessions() = sessions.values.toList()
}