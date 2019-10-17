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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.IntRange
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

const val KEY_ALARM_CONFIG_ID = "alarm_config_id"

class AlarmManager(private val context: Context) {
    interface Listener {
        fun onUpdate()
    }
    var listener: Listener? = null
    private val store = AlarmConfigStore(context)
    private val alarmService = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val dayMap = mapOf(
        Calendar.MONDAY to "monday",
        Calendar.TUESDAY to "tuesday",
        Calendar.WEDNESDAY to "wednesday",
        Calendar.THURSDAY to "thursday",
        Calendar.FRIDAY to "friday",
        Calendar.SATURDAY to "saturday",
        Calendar.SUNDAY to "sunday"
    )

    fun add(alarmConfig: AlarmConfig) {
        Log.w(TAG(), "alarmConfig: ${alarmConfig.id}")
        //One AlarmConfig can lead to 1 or more Alarms (each alarm is a day of the week set to repeat weekly)
        Date.from(Instant.now()).let { now ->
            alarmConfig.dayCalendarIds.forEach { dayCalendarId ->
                val alarmCalendar = createCalendar(now, alarmConfig.time, dayCalendarId)
                Log.w(TAG(), "alarm: ${formateDate(alarmCalendar.time)}")
                alarmService.setRepeating(
                    AlarmManager.RTC,
                    alarmCalendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7, //weekly repeating
                    getPendingIntent(alarmConfig, dayCalendarId)
                )
            }
        }
        store.add(alarmConfig)
    }

    fun remove(alarmConfig: AlarmConfig, removeFromStore: Boolean = true) {
        alarmConfig.dayCalendarIds.forEach {
            alarmService.cancel(getPendingIntent(alarmConfig, it))
        }
        if (removeFromStore) {
            store.remove(alarmConfig)
        }
    }

    fun removeAll() {
        store.getAll().forEach {
            remove(it, false)
        }
        store.removeAll()
    }

    fun getAll(): List<AlarmConfig> = store.getAll()

    fun onAlarm(alarmConfigId: String) {
        val alarmConfig = store.get(alarmConfigId)
        alarmConfig?.let {
            it.timestampReceived = System.currentTimeMillis()
            store.update(it)
            mainScope.launch {
                listener?.onUpdate()
            }
        }
    }

    private fun getPendingIntent(alarmConfig: AlarmConfig, @IntRange(from = 1, to = 7) dayCalendarId: Int): PendingIntent {
        return Intent(
            createAlarmId(alarmConfig, dayCalendarId),
            null,
            context,
            AlarmReceiver::class.java
        ).run {
            putExtra(KEY_ALARM_CONFIG_ID, alarmConfig.id)
            PendingIntent.getBroadcast(context, 0, this, 0)
        }
    }

    private fun createAlarmId(alarmConfig: AlarmConfig, @IntRange(from = 1, to = 7) dayCalendarId: Int): String {
        return "${context.packageName}.alarm.${alarmConfig.id}.${dayMap[dayCalendarId]}.${alarmConfig.time}"
            .also { Log.w(TAG(), "id: $it") }
    }

    private fun createCalendar(now: Date, alarmTime: Time, day: Int): Calendar {
        return Calendar.getInstance().apply {
            time = now
            set(Calendar.DAY_OF_WEEK, day)
            set(Calendar.HOUR_OF_DAY, alarmTime.hour)
            set(Calendar.MINUTE, alarmTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (time.before(now)) {
                add(Calendar.DAY_OF_MONTH, 7)
            }
        }
    }
}

