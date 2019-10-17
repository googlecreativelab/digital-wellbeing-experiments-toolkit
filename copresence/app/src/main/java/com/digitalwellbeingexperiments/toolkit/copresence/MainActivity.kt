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

package com.digitalwellbeingexperiments.toolkit.copresence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), DataStore.Listener {

    private var nearbyDeviceSessionAdapter = NearbyDeviceSessionAdapter()
    private lateinit var recycler: RecyclerView
    private lateinit var nearbyManager: NearbyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = nearbyDeviceSessionAdapter
            recycler = this
        }
        findViewById<TextView>(R.id.device_name_value).apply {
            text = getDataStore().getName()
        }

        nearbyManager = NearbyManager(this)
    }

    override fun onStart() {
        super.onStart()
        nearbyDeviceSessionAdapter.clearData()
        getDataStore().listener = this
        nearbyManager.start()
    }

    override fun onStop() {
        getDataStore().listener = null
        nearbyManager.stop()
        super.onStop()
    }

    override fun onSessionListUpdated() {
        nearbyDeviceSessionAdapter.setData(getDataStore().getAllSessions())
    }
}
