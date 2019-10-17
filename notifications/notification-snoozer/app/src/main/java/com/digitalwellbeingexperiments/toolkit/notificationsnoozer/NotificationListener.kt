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

package com.digitalwellbeingexperiments.toolkit.notificationsnoozer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class NotificationListener : NotificationListenerService() {
    companion object {
        private const val SNOOZE_DURATION_MS: Long = 1000 * 60 * 1 //ONE MINUTE

        //ACTION (UI --> Service)
        const val ACTION_INIT = "init"
        const val ACTION_SNOOZE = "snooze"
        const val KEY_NOTIFICATION_ID = "key_notification_id"

        //ACTION (Service --> UI)
        const val ACTION_REFRESH_UI = "refresh"

        const val TAG = "NotificationListener"
    }

    //Handles messages from UI (Activity) to background service
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.w(TAG, "onReceive() ${intent?.action}")
            when (intent?.action) {
                ACTION_INIT -> {
                    refresh()
                }
                ACTION_SNOOZE -> {
                    intent.extras?.let { extra ->
                        extra.getString(KEY_NOTIFICATION_ID)?.let { sbnKeyToSnooze ->
                            NotificationsData.getItem(sbnKeyToSnooze)?.let {
                                it.snoozeEndTime = System.currentTimeMillis() + SNOOZE_DURATION_MS
                                snoozeNotification(sbnKeyToSnooze, SNOOZE_DURATION_MS)
                                sendUpdate()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onListenerConnected() {
        Log.w(TAG, "onListenerConnected()")

        val filter = IntentFilter()
        filter.addAction(ACTION_INIT)
        filter.addAction(ACTION_SNOOZE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        if(NotificationsData.getAll().isEmpty()){
            //might be first connection, grab active notifications and send to UI
            refresh()
        }
    }

    override fun onListenerDisconnected() {
        Log.w(TAG, "onListenerDisconnected()")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (this.packageName != sbn.packageName) {
            Log.w(TAG, "onNotificationPosted(sbn) ${sbn.key}")

            NotificationsData.getItem(sbn.key)?.let {
                Log.w(
                    TAG,
                    "UNSNOOZE ${it.sbnKey} snoozeEndTime: ${getFormattedSnoozeEndTime(it.snoozeEndTime)}"
                )
            }

            NotificationsData.addItem(sbn)
            sendUpdate()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (this.packageName != sbn?.packageName) {
            Log.w(TAG, "onNotificationRemoved(sbn) ${sbn?.key}")
            sbn?.let { statusBarNotification ->
                NotificationsData.getItem(statusBarNotification.key)?.let {
                    if (it.snoozeEndTime == UNSNOOZED) {
                        Log.w(TAG(), "\t REMOVE!")
                        //if it's not snoozed, remove it
                        NotificationsData.removeItem(it.sbnKey)
                        sendUpdate()
                    }
                }
            }
        }
    }

    private fun refresh(){
        NotificationsData.init(
            activeNotifications
                .filter { sbn -> sbn.packageName != this.packageName }
                .toList()
        )
        sendUpdate()
    }

    private fun sendUpdate() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTION_REFRESH_UI))
    }

    private fun getFormattedSnoozeEndTime(snoozeEndTime: Long): String {
        return if (snoozeEndTime == UNSNOOZED) {
            "UNSNOOZED"
        } else {
            formatTimestamp(snoozeEndTime)
        }
    }
}
