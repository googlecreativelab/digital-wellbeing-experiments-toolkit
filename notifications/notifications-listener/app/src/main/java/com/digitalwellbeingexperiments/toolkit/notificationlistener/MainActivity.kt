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

import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    //Handles messages from background service to UI(Activity)
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.w(TAG(), "onReceive() ${intent?.action}")
            intent?.extras?.let {
                val data = it.getString(NotificationListener.KEY_NOTIFICATIONS)
                data?.let { jsonString ->
                    val gson = Gson()
                    val notifs = gson.fromJson(jsonString, Array<NotificationItem>::class.java)
                    notificationListAdapter.setData(notifs.toList())
                }
            }
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
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                receiver,
                IntentFilter(NotificationListener.ACTION_REFRESH_UI)
            )
        //check for notification permission
        if (!NotificationListenerPermissionHelper.listenerEnabled(this)) {
            showPromptNotificationAccess()
        } else {
            setNotificationListenerService(true)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        if (NotificationListenerPermissionHelper.listenerEnabled(this)) {
            setNotificationListenerService(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear_button -> {
                LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(NotificationListener.ACTION_CLEAR_DATA))
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun setNotificationListenerService(isEnabled: Boolean) {
        val notificationListenerService = ComponentName(this, NotificationListener::class.java)
        val currentState = packageManager.getComponentEnabledSetting(notificationListenerService)
        val newState = if (isEnabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        Log.w(TAG(), "setNotificationListenerService() $currentState -> $newState")

        packageManager.setComponentEnabledSetting(
            notificationListenerService,
            newState,
            PackageManager.DONT_KILL_APP
        )

    }
}
