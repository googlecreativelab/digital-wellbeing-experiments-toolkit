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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class NotificationListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<NotificationItem>()

    fun setData(newItems: List<NotificationItem>) {
        items.clear()
        items.addAll(newItems)
        items.sortBy { it.postTime }
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
            findViewById<TextView>(R.id.notification_timestamp).text = formatTimestamp(item.postTime)
            findViewById<TextView>(R.id.notification_title).text = item.title
            findViewById<TextView>(R.id.notification_message).text = item.text
        }
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)