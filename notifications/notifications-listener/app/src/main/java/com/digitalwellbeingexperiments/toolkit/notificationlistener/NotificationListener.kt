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

package com.digitalwellbeingexperiments.toolkit.notificationlistener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson

class NotificationListener : NotificationListenerService() {
    companion object {
        const val ACTION_REFRESH_UI = "REFRESH"
        const val ACTION_CLEAR_DATA = "CLEAR"
        const val KEY_NOTIFICATIONS = "key_notifications"
    }
    //Handles messages from UI (Activity) to background service
    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.w(TAG(), "onReceive() ${intent?.action}")
            dataMap.clear()
            sendUpdate()
        }
    }

    private val dataMap = HashMap<String, NotificationItem>()

    //when user switches ON in Settings
    override fun onListenerConnected() {
        Log.w(TAG(), "onListenerConnected()")
        val currentNotifications = activeNotifications
            .filter { sbn -> sbn.packageName != this.packageName }
            .map {
                createNotificationItem(it)
            }.toTypedArray()
        currentNotifications.forEach {
            dataMap[it.sbnKey] = it
        }
        sendUpdate()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
            IntentFilter(ACTION_CLEAR_DATA)
        )
    }

    //when user switches OFF in Settings
    override fun onListenerDisconnected() {
        Log.w(TAG(), "onListenerDisconnected()")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (this.packageName != sbn.packageName) {
            Log.w(TAG(), "onNotificationPosted() ${sbn.key}")
            val notificationItem = createNotificationItem(sbn)
            dataMap[sbn.key] = notificationItem
            sendUpdate()
        }
    }

    private fun sendUpdate() {
        val intent = Intent(ACTION_REFRESH_UI)
        val dataList = dataMap.values.toList()
        intent.putExtra(KEY_NOTIFICATIONS, Gson().toJson(dataList))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationItem(sbn: StatusBarNotification): NotificationItem {
        return NotificationItem(
            sbn.key,
            sbn.packageName,
            title = "${sbn.getTitleBig()}\n${sbn.getTitle()}".trim(),
            text = "${sbn.getText()}\n${sbn.getSubText()}".trim(),
            postTime = sbn.postTime
        )
    }
}
