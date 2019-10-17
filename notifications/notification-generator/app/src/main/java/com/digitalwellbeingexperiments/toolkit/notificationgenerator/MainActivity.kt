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

package com.digitalwellbeingexperiments.toolkit.notificationgenerator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import com.digitalwellbeingexperiments.toolkit.notificationgenerator.NotificationGenerationService.Companion.BASE_INTERVAL_MS
import com.digitalwellbeingexperiments.toolkit.notificationgenerator.NotificationGenerationService.Companion.KEY_INTERVAL_MS

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = Preferences(this)

        //grab views
        val frequencySlider = findViewById<SeekBar>(R.id.frequency_slider)
        val frequencyValue = findViewById<TextView>(R.id.value_frequency)
        val enableNotificationsCheckBox = findViewById<CheckBox>(R.id.notifications_enabled_checkbox)

        frequencySlider.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.get().edit().putInt(INTERVAL_PREFERENCE, progress).apply()
                frequencyValue.text = getString(R.string.frequency_value, (getFrequencyMillis(progress))/1000)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        frequencySlider.progress = preferences.get().getInt(INTERVAL_PREFERENCE, 0)
        frequencyValue.text = getString(R.string.frequency_value, (getFrequencyMillis(frequencySlider.progress))/1000)

        enableNotificationsCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                preferences.get().edit().putBoolean(ENABLED_PREFERENCE, isChecked).apply()

                //we don't change notif frequency on the fly
                frequencySlider.isEnabled = !isChecked

                if(isChecked){
                    val startIntent = Intent(this@MainActivity, NotificationGenerationService::class.java)
                    startIntent.putExtra(KEY_INTERVAL_MS, getFrequencyMillis(frequencySlider.progress))
                    startService(startIntent)
                } else {
                    val stopIntent = Intent(this@MainActivity, NotificationGenerationService::class.java)
                    stopService(stopIntent)
                }
            }

            isChecked = preferences.get().getBoolean(ENABLED_PREFERENCE, false)
        }
    }

    private fun getFrequencyMillis(progress:Int) = BASE_INTERVAL_MS + (progress * BASE_INTERVAL_MS)
}
