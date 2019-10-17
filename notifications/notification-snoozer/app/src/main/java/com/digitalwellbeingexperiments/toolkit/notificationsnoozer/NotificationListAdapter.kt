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

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView


class NotificationListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<NotificationItem>()

    fun setData(newItems: List<NotificationItem>) {
//        Log.w(TAG(), "setData()")
//        newItems.forEachIndexed { index, item ->
//            Log.w(NotificationListener.TAG, "\t $index) ${item.sbnKey}")
//        }
        newItems.sortedBy { it.postTime }
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_notification,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.app_name).text = context.packageManager.getApplicationLabel(item.packageName)
            findViewById<TextView>(R.id.notification_title).text = item.title
            findViewById<TextView>(R.id.notification_message).text = item.text
            findViewById<TextView>(R.id.notification_received_time).text = context.getString(R.string.label_received, formatTimestamp(item.postTime))

            if(item.snoozeEndTime == UNSNOOZED) {
                //not snoozed, so allow snoozing
                findViewById<TextView>(R.id.notification_due_time).apply {
                    text = ""
                    visibility = View.GONE
                }
                findViewById<View>(R.id.btn_snooze).apply {
                    setOnClickListener {
                        sendSnoozeNotificationMessage(context, item.sbnKey)
                    }
                    visibility = View.VISIBLE
                }

                findViewById<TextView>(R.id.notification_received_time).visibility = View.VISIBLE
            } else {
                //snoozed, disabled snoozing
                findViewById<TextView>(R.id.notification_due_time).apply {
                    text = context.getString(R.string.label_snoozed_until, formatTimestamp(item.snoozeEndTime))
                    visibility = View.VISIBLE
                }

                findViewById<View>(R.id.btn_snooze).apply {
                    setOnClickListener(null)
                    visibility = View.INVISIBLE
                }
                findViewById<TextView>(R.id.notification_received_time).visibility = View.GONE
            }
        }
    }

    private fun sendSnoozeNotificationMessage(context: Context, notificationId: String) {
//        Log.w(TAG(), "sendSnoozeNotificationMessage() $notificationId")
        val intent = Intent(NotificationListener.ACTION_SNOOZE)
        intent.putExtra(NotificationListener.KEY_NOTIFICATION_ID, notificationId)
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(intent)
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)