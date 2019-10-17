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

package com.digitalwellbeingexperiments.toolkit.alarmscheduler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TimePicker
import android.widget.ToggleButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import java.util.*


class SetupFragment : Fragment(){
    private val daysViewToDataMap = mapOf(
        R.id.btn_mon to Calendar.MONDAY,
        R.id.btn_tues to Calendar.TUESDAY,
        R.id.btn_weds to Calendar.WEDNESDAY,
        R.id.btn_thurs to Calendar.THURSDAY,
        R.id.btn_fri to Calendar.FRIDAY,
        R.id.btn_sat to Calendar.SATURDAY,
        R.id.btn_sun to Calendar.SUNDAY
    )

    private lateinit var timePicker: TimePicker
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup, container, false).apply {
            timePicker = findViewById(R.id.time_picker)
            findViewById<Button>(R.id.btn_add_alarm).setOnClickListener {
                saveAlarm()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //block back button
        val backCallback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                //block
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(backCallback)
    }

    private fun getPickerTime() = Time(timePicker.hour, timePicker.minute)

    private fun getSelectedDays(): List<Int> {
        val list = ArrayList<Int>()
        daysViewToDataMap.entries.forEach { entry ->
            if (requireView().findViewById<ToggleButton>(entry.key).isChecked) {
                list.add(entry.value)
            }
        }
        return list
    }

    private fun saveAlarm(){
        if(getSelectedDays().isEmpty()) {
            createSnackbar(requireView(), getString(R.string.error_select_day), Snackbar.LENGTH_LONG).show()
        } else {
            val alarmConfig = AlarmConfig(getPickerTime(), getSelectedDays())
            AlarmSchedulerApplication.instance.alarmManager.add(alarmConfig)
            Navigation.findNavController(requireView()).popBackStack()
        }
    }
}