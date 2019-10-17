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

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val appListAdapter = AppListAdapter()
    private var showAllApps = false
    lateinit var recycler: RecyclerView
    lateinit var loading: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = appListAdapter
            recycler = this
        }

        loading = findViewById(R.id.loading)

        findViewById<View>(R.id.filter).setOnClickListener {
            if (findViewById<RecyclerView>(R.id.recycler).visibility == View.VISIBLE) {
                showAllApps = !showAllApps
                refreshData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        findViewById<View>(R.id.filter_status).alpha = if (showAllApps) 1.0f else 0.2f
        recycler.visibility = View.INVISIBLE
        loading.visibility = View.VISIBLE

        val context = this
        bgScope.launch {
            val apps =
                if (showAllApps) packageManager.getAllPackages(context) else packageManager.getLaunchablePackages(
                    context
                )
            mainScope.launch {
                appListAdapter.setData(context, apps)
                recycler.visibility = View.VISIBLE
                loading.visibility = View.INVISIBLE
            }
        }
    }
}
