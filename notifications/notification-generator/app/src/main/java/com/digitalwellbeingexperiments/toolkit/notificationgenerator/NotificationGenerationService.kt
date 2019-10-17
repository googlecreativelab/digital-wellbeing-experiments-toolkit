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

package com.digitalwellbeingexperiments.toolkit.notificationgenerator

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

class NotificationGenerationService : Service() {
    companion object {
        const val BASE_INTERVAL_MS = 15000L
        const val KEY_INTERVAL_MS = "interval_ms"
    }

    private lateinit var notificationGenerator: NotificationGenerator
    private var preferences: Preferences = Preferences(this)
    private val t = Timer()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        notificationGenerator = NotificationGenerator(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val intervalMillis:Long = intent.extras?.getLong(KEY_INTERVAL_MS, BASE_INTERVAL_MS)?:BASE_INTERVAL_MS
//        Log.w(TAG(), "onStartCommand() interval millis $intervalMillis")

        t.scheduleAtFixedRate(generateTimerTask(),0L, intervalMillis)
        return START_STICKY
    }

    private fun generateTimerTask(): TimerTask {
        return object : TimerTask() {
            override fun run() {
                if(preferences.get().getBoolean(ENABLED_PREFERENCE, false)) notificationGenerator.generate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
