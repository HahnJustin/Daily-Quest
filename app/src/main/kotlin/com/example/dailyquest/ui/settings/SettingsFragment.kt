package com.example.dailyquest.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.dailyquest.R
import com.example.dailyquest.models.DataContainer
import com.example.dailyquest.utils.JsonManager
import java.time.LocalTime
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.preference.SeekBarPreference
import com.example.dailyquest.NotificationReceiver
import com.example.dailyquest.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {

    var jsonManager : JsonManager? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        jsonManager = JsonManager(requireContext())

        // Start Day Time
        val timePreference: Preference? = findPreference("start_day_time")
        timePreference?.setOnPreferenceChangeListener { preference, newValue ->
            // Save the new start time in your DataContainer
            saveStartDayTime(newValue as String)
            true
        }

        // Variance Pref
        val variancePreference = findPreference<SeekBarPreference>("variance_intensity")
        variancePreference?.setOnPreferenceChangeListener { preference, newValue ->
            // newValue is an integer (0 - 20)
            val floatValue = (newValue as Int) / 10.0f  // Convert the integer to a float (0.0 to 2.0)
            preference.summary = "Current variance intensity: $floatValue"
            true  // Return true to update the value
        }

        val initialValue = variancePreference?.value?.div(10.0f)
        variancePreference?.summary = "Current variance intensity: $initialValue"

        //DayOff Pref
        val dayOffPreference = findPreference<SeekBarPreference>("percent_day_off")
        dayOffPreference?.setOnPreferenceChangeListener { preference, newValue ->
            // newValue is an integer (0 - 100)
            preference.summary = "Current chance of a day off: $newValue%"
            true  // Return true to update the value
        }
        val initialDayOffValue = dayOffPreference?.value
        dayOffPreference?.summary = "Current chance of a day off: $initialDayOffValue%"

        // Delete streak and etc pref
        val resetPreference: Preference? = findPreference("reset_data_container")
        resetPreference?.setOnPreferenceClickListener {
            showResetDataContainerDialog()
            true
        }

        // Delete database
        val deletePreference: Preference? = findPreference("delete_database")
        deletePreference?.setOnPreferenceClickListener {
            showDeleteDatabaseDialog()
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

    private fun showResetDataContainerDialog() {
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

    private fun showDeleteDatabaseDialog() {
        Log.d("SettingsFragment", "Deleting Data Container")

        // Show a confirmation dialog before deleting the task
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Database")
            .setMessage("Are you sure you want to delete all your quests?")
            .setPositiveButton("Yes") { dialog, _ ->

                val db = AppDatabase.getDatabase(requireContext())
                val taskDao = db.taskDao()

                CoroutineScope(Dispatchers.Default).launch {
                    taskDao.deleteAllTasks()
                    triggerTaskMaintenance()
                    dialog.dismiss()
                }
                Toast.makeText(context, "Database deleted!", Toast.LENGTH_SHORT).show()
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
