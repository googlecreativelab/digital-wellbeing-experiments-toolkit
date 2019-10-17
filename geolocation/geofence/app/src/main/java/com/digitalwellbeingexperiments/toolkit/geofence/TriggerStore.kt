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
import com.google.gson.Gson

class TriggerStore(context: Context) {

    companion object {
        private const val PREF_NAME = "datastore"
        private const val KEY_TRIGGERS = "triggers"
    }

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun add(trigger: Trigger) {
        saveAll(getAll() + trigger)
    }

    fun remove(trigger: Trigger) {
        saveAll(getAll() - trigger)
    }

    fun update(trigger: Trigger){
        get(trigger.id)?.let {
            remove(it)
        }
        add(trigger)
    }

    fun getAll(): List<Trigger> = if (prefs.contains(KEY_TRIGGERS)) {
        val triggers =
            gson.fromJson(prefs.getString(KEY_TRIGGERS, null), Array<Trigger>::class.java)
        triggers?.toList() ?: listOf()
    } else {
        listOf()
    }

    fun get(requestId: String?) = getAll().firstOrNull { it.id == requestId }

    private fun saveAll(triggers: List<Trigger>) =
        prefs.edit()
            .putString(KEY_TRIGGERS, gson.toJson(triggers))
            .apply()
}