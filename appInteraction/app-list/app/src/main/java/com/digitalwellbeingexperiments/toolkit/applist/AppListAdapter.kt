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

package com.digitalwellbeingexperiments.toolkit.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = ArrayList<AppItem>()

    fun setData(context: Context, newItems: List<ApplicationInfo>){
        items.clear()
        items.addAll(newItems.map {
            val item = AppItem(
                context.packageManager.getApplicationLabel(it.packageName),
                it.packageName,
                context.packageManager.getApplicationIcon(it)
            )
            item })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_app,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val appItem = items[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.app_name).text = appItem.name
            findViewById<TextView>(R.id.app_package).text = appItem.packageName
            findViewById<ImageView>(R.id.app_icon).setImageDrawable(appItem.icon)
        }
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)