package com.example.dailyquest.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TimePicker
import androidx.preference.DialogPreference
import com.example.dailyquest.utils.JsonManager

class TimePickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    private var time: String? = null

    override fun onBindViewHolder(holder: androidx.preference.PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        // Create a time picker dialog and handle the time selection logic
        val timePicker = TimePicker(context)
        timePicker.setIs24HourView(true)

        // Set the initial time to the current persisted time
        time?.let {
            val parts = it.split(":")
            if (parts.size == 2) {
                timePicker.hour = parts[0].toInt()
                timePicker.minute = parts[1].toInt()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(timePicker)
            .setPositiveButton("OK") { _, _ ->
                val hour = timePicker.hour
                val minute = timePicker.minute
                val newTime = String.format("%02d:%02d", hour, minute)

                // Notify the listener that the value is about to change
                if (callChangeListener(newTime)) {
                    time = newTime
                    persistString(newTime)  // Persist the new value
                    summary = time  // Update the summary of the preference
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val jsonManager = JsonManager(context)
        val dataContainer = jsonManager.loadData()
        time = dataContainer?.startDayTime.toString()
        summary = time  // Set the summary to the current time
    }
}

