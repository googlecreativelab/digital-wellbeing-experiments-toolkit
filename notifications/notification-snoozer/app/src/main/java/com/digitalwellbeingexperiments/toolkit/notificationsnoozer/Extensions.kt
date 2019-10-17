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

import android.app.Notification
import android.service.notification.StatusBarNotification
import java.text.SimpleDateFormat
import java.util.*

fun Any.TAG() = this.javaClass.simpleName

fun StatusBarNotification.getText(): String = (this.notification.extras.get(Notification.EXTRA_TEXT) ?: "").toString()

fun StatusBarNotification.getTitle(): String = (this.notification.extras.get(Notification.EXTRA_TITLE) ?: "").toString()

fun StatusBarNotification.getTitleBig(): String = (this.notification.extras.get(Notification.EXTRA_TITLE_BIG) ?: "").toString()

fun StatusBarNotification.getSubText(): String = (this.notification.extras.get(Notification.EXTRA_SUB_TEXT) ?: "").toString()

fun formatTimestamp(timestamp: Long, pattern: String = "EEE, dd MMM yyyy, HH:mm:ss"): String {
    val sdf = SimpleDateFormat(pattern)
    val resultdate = Date(timestamp)
    return sdf.format(resultdate)
}