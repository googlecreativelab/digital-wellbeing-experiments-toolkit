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

package com.digitalwellbeingexperiments.toolkit.geofence


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TriggersAdapter :
    RecyclerView.Adapter<TriggerViewHolder>() {
    private val triggerManager = GeofenceApplication.instance.triggerManager
    private var triggers = triggerManager.getAll()

    init {
        triggerManager.listener = object : TriggerManager.Listener {
            override fun onUpdate() {
                refresh()
            }
        }
    }

    fun refresh() {
        triggers = triggerManager.getAll()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TriggerViewHolder {
        return TriggerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_trigger,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = triggers.size

    override fun onBindViewHolder(holder: TriggerViewHolder, position: Int) {
        val item = triggers[position]
        Log.w(TAG(), "onBindViewHolder(${item.placeName})")
        holder.itemView.apply {
            findViewById<CheckBox>(R.id.checkbox).isChecked = (item.triggerTimestamp != null)
            findViewById<TextView>(R.id.sub_label).apply {
                this.text = item.triggerTimestamp?.let {
                    context.getString(R.string.last_triggered, formatTimestamp(it))
                }?:run {
                    ""
                }
            }
            findViewById<TextView>(R.id.label).text = item.placeName
            findViewById<View>(R.id.delete).setOnClickListener {
                triggerManager.remove(item) {
                    refresh()
                }
            }
        }
    }
}

class TriggerViewHolder(view: View) : RecyclerView.ViewHolder(view)