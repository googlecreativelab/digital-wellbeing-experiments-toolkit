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

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    //Handles messages from background service to UI(Activity)
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.w(TAG(), "onReceive() ${intent?.action}")
            notificationListAdapter.setData(NotificationsData.getAll())
        }
    }

    private val notificationListAdapter = NotificationListAdapter()
    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            adapter = notificationListAdapter
            recycler = this
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                receiver,
                IntentFilter(NotificationListener.ACTION_REFRESH_UI)
            )
    }

    override fun onResume() {
        super.onResume()

        //check for notification permission
        if (!NotificationListenerPermissionHelper.listenerEnabled(this)) {
            showPromptNotificationAccess()
        } else {
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(NotificationListener.ACTION_INIT))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun showPromptNotificationAccess() {
        val builder = AlertDialog.Builder(this).apply {
            setTitle(R.string.prompt_notification_setting_title)
            setMessage(R.string.prompt_notification_setting_message)
            setCancelable(false)
            setPositiveButton(R.string.go_settings) { dialog, _ ->
                NotificationListenerPermissionHelper.gotoNotificationSettings(this@MainActivity)
                dialog.dismiss()
            }
        }

        builder.create().show()
    }
}
