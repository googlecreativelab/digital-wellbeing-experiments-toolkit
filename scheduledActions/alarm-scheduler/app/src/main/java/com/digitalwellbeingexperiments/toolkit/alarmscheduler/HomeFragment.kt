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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import java.util.*

class HomeFragment : Fragment() {

    private val dayCalIdToViewMap = mapOf(
         Calendar.MONDAY to R.id.btn_mon,
         Calendar.TUESDAY to R.id.btn_tues,
         Calendar.WEDNESDAY to R.id.btn_weds,
         Calendar.THURSDAY to R.id.btn_thurs,
         Calendar.FRIDAY to R.id.btn_fri,
         Calendar.SATURDAY to R.id.btn_sat,
         Calendar.SUNDAY to R.id.btn_sun
    )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            findViewById<Button>(R.id.btn_clear_alarm).setOnClickListener {
                AlarmSchedulerApplication.instance.alarmManager.removeAll()
                showSetup()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val appInstance = AlarmSchedulerApplication.instance
        val alarmManager = AlarmSchedulerApplication.instance.alarmManager
        if(alarmManager.getAll().isEmpty()) {
            //no alarms, show setup
            showSetup()
        } else {
            alarmManager.listener = object: AlarmManager.Listener {
                override fun onUpdate() {
                    refresh()
                }
            }
            refresh()
            if(!appInstance.hasShownCloseAppPrompt()){
                showPromptCloseApp()
            }
        }
    }

    private fun showSetup(){
        val navigation = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        navigation.navigate(R.id.action_homeFragment_to_setupFragment)
    }

    private fun showPromptCloseApp(){
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.prompt_alarm_set_title)
            setMessage(R.string.prompt_alarm_set_message)
            setCancelable(false)
            setPositiveButton(android.R.string.yes) { dialog, _ ->
                AlarmSchedulerApplication.instance.setShownCloseAppPrompt()
                dialog.dismiss()
            }
        }

        builder.create().show()
    }

    private fun showPromptAlarmReceived(timestamp: Long){
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.prompt_alarm_received_title)
            setMessage(getString(R.string.prompt_alarm_received_message, formatTimestamp(timestamp)))
            setCancelable(false)
            setPositiveButton(android.R.string.yes) { dialog, _ ->
                AlarmSchedulerApplication.instance.setShownLastAlarmPrompt(timestamp)
                dialog.dismiss()
            }
        }

        builder.create().show()
    }

    private fun refresh(){
        //read data and bind to views
        AlarmSchedulerApplication.instance.alarmManager.getAll().firstOrNull()?.let { currentAlarmConfig ->
            //Alarm: Time
            requireView().findViewById<TextView>(R.id.subtitle).text = currentAlarmConfig.time.toString()
            //Alarm: Days
            refreshDays(currentAlarmConfig)

            //timestamp
            currentAlarmConfig.timestampReceived?.let {
                requireView().findViewById<TextView>(R.id.timestamp).apply {
                    text = getString(R.string.last_triggered, formatTimestamp(it))
                }
            }?:run {
                requireView().findViewById<TextView>(R.id.timestamp).apply {
                    text = getString(R.string.last_triggered, getString(R.string.dash))
                }
            }
            //check for prompt
            currentAlarmConfig.timestampReceived?.let {
                if(!AlarmSchedulerApplication.instance.hasShownLastAlarmPrompt(it)){
                    showPromptAlarmReceived(it)
                }
            }
        }
    }

    private fun refreshDays(alarmConfig: AlarmConfig){
        alarmConfig.dayCalendarIds.forEach { dayCalId ->
            dayCalIdToViewMap[dayCalId]?.let { viewId ->
                requireView().findViewById<ToggleButton>(viewId).isChecked = true
            }

        }
    }
}