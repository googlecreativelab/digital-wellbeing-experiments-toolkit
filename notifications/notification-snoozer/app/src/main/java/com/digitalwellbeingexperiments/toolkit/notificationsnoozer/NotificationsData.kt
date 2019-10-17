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

import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationsData {

    companion object {
        private val dataMap = HashMap<String, NotificationItem>()

        fun init(list: List<StatusBarNotification>) {
            list.forEach {
                dataMap[it.key] = createNotificationItem(it)
            }
        }

        fun addItem(sbn: StatusBarNotification){
            val notificationItem = createNotificationItem(sbn)
            dataMap[sbn.key]?.let {
                Log.w(NotificationListener.TAG, "UNSNOOZE ${it.sbnKey} snoozeEndTime: ${getFormattedSnoozeEndTime(notificationItem.snoozeEndTime)}")
            }
            //unsnoozed... reset snoozeEndTime

            dataMap[sbn.key] = notificationItem

        }

        fun removeItem(key: String) {
            dataMap.remove(key)
        }

        fun getItem(key: String) = dataMap[key]

        fun clearAll() {
            dataMap.clear()
        }

        fun getAll(): List<NotificationItem> {
            return dataMap.values.toList()
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

        private fun getFormattedSnoozeEndTime(snoozeEndTime: Long): String {
            return if(snoozeEndTime == UNSNOOZED){
                "UNSNOOZED"
            } else {
                formatTimestamp(snoozeEndTime)
            }
        }
    }
}