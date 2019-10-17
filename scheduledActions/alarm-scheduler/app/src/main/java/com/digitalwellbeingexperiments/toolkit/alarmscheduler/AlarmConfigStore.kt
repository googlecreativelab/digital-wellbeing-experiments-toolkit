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

package com.digitalwellbeingexperiments.toolkit.alarmscheduler

import android.content.Context
import com.google.gson.Gson

class AlarmConfigStore(context: Context) {
    companion object {
        private const val KEY_ALARMS = "alarms"
    }


    private val prefs = context.getSharedPreferences(AlarmSchedulerApplication.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun add(alarmConfig: AlarmConfig) {
        saveAll(getAll() + alarmConfig)
    }

    fun remove(alarmConfig: AlarmConfig) {
        saveAll(getAll() - alarmConfig)
    }

    fun removeAll(){
        prefs.edit().clear().apply()
    }

    fun update(alarmConfig: AlarmConfig){
        get(alarmConfig.id)?.let {
            remove(it)
        }
        add(alarmConfig)
    }

    fun get(alarmConfigId: String) = getAll().firstOrNull { it.id == alarmConfigId }

    fun getAll(): List<AlarmConfig> = if (prefs.contains(KEY_ALARMS)) {
        val alarms =
            gson.fromJson(prefs.getString(KEY_ALARMS, null), Array<AlarmConfig>::class.java)
        alarms?.toList() ?: listOf()
    } else {
        listOf()
    }

    private fun saveAll(alarmConfigs: List<AlarmConfig>) =
        prefs.edit()
            .putString(KEY_ALARMS, gson.toJson(alarmConfigs))
            .apply()
}