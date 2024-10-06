package com.example.dailyquest.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.dailyquest.R
import com.example.dailyquest.database.Task
import com.example.dailyquest.models.DataContainer
import com.example.dailyquest.utils.JsonManager
import java.time.LocalDateTime
import java.time.LocalTime
import android.content.BroadcastReceiver
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.dailyquest.NotificationReceiver

class SettingsFragment : PreferenceFragmentCompat() {

    var jsonManager : JsonManager? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        jsonManager = JsonManager(requireContext())

        // Handle the time picker change
        val timePreference: Preference? = findPreference("start_day_time")
        timePreference?.setOnPreferenceChangeListener { preference, newValue ->
            // Save the new start time in your DataContainer
            saveStartDayTime(newValue as String)
            true
        }

        // Handle the reset button
        val resetPreference: Preference? = findPreference("reset_data_container")
        resetPreference?.setOnPreferenceClickListener {
            resetDataContainer()
            true
        }
    }

    private fun saveStartDayTime(newTime: String) {
        Log.d("SettingsFragment", "Saving new Start day time $newTime")
        // Save the new start day time in your DataContainer
        // Load the DataContainer and update the startDayTime field
        val dataContainer = jsonManager?.loadData()
        dataContainer?.startDayTime = LocalTime.parse(newTime)
        // Save the updated dataContainer (e.g., in SharedPreferences, database, etc.)
        jsonManager?.saveData(dataContainer!!)
        triggerTaskMaintenance()
    }

    private fun resetDataContainer() {
        Log.d("SettingsFragment", "Deleting Data Container")

        // Show a confirmation dialog before deleting the task
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Streak and Preference Data")
            .setMessage("Are you sure you want to delete your streak and preference data?")
            .setPositiveButton("Yes") { dialog, _ ->
                // User confirmed deletion, proceed with task deletion
                // Delete the current DataContainer and create a new one
                val newContainer = DataContainer()
                jsonManager?.saveData(newContainer)
                Toast.makeText(context, "Save data reset!", Toast.LENGTH_SHORT).show()
                triggerTaskMaintenance()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                // User canceled, just dismiss the dialog
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun triggerTaskMaintenance(){
        val intent = Intent().apply {
            action = "com.example.dailyquest.CHANGED_PREF"
            setClass(requireContext(), NotificationReceiver::class.java)
        }
        requireContext().sendBroadcast(intent)
        Log.d("SettingsFragment", "Broadcasting custom intent: $intent")
    }
}
