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

import android.app.Application
import android.content.Context

class AlarmSchedulerApplication: Application() {
    companion object {
        const val PREF_NAME = "datastore"
        private const val KEY_PROMPT_CLOSE_APP = "prompt_close_app"
        private const val KEY_PROMPT_LAST_ALARM = "prompt_last_alarm"


        lateinit var instance: AlarmSchedulerApplication
            private set //Safety: only this class can set the instance
    }
    lateinit var alarmManager: AlarmManager

    override fun onCreate() {
        super.onCreate()
        alarmManager = AlarmManager(applicationContext)
        instance = this
    }

    fun hasShownCloseAppPrompt()
        = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PROMPT_CLOSE_APP, false)

    fun setShownCloseAppPrompt(){
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PROMPT_CLOSE_APP, true)
            .apply()
    }

    fun hasShownLastAlarmPrompt(timestamp: Long):Boolean {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastAlarmPrompted = sharedPref.getLong(KEY_PROMPT_LAST_ALARM, -1000)
        return lastAlarmPrompted == timestamp
    }


    fun setShownLastAlarmPrompt(timestamp: Long){
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_PROMPT_LAST_ALARM, timestamp)
            .apply()
    }
}