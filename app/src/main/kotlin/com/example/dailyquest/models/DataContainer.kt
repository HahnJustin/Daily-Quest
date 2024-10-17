package com.example.dailyquest.models

import android.os.Build
import android.text.BoringLayout
import androidx.annotation.RequiresApi
import com.example.dailyquest.database.Task
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.LocalTime

data class DataContainer(
    @SerializedName("last_date_lapsed") var lastDateLapsed: LocalDateTime,
    @SerializedName("day_start_time") var startDayTime: LocalTime,
    @SerializedName("current_task") var currentTask: Task?,
    @SerializedName("task_generated_time") var taskGeneratedTime: LocalDateTime?,
    @SerializedName("delayed_task") var  delayedTask: Boolean
) {

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val DEFAULT_START_TIME: LocalTime = LocalTime.parse("07:00")
    }

    // Secondary constructor
    @RequiresApi(Build.VERSION_CODES.O)
    constructor() : this(
        lastDateLapsed = DEFAULT_START_TIME.atDate(LocalDateTime.now().toLocalDate()),
        startDayTime = DEFAULT_START_TIME,
        currentTask = null,
        taskGeneratedTime = null,
        delayedTask = false
    )

    override fun toString() : String{
        return "[DataContainer]: \n" +
        "  StreakStart: " + lastDateLapsed.toString() + "\n" +
        "  StartDayTime: " + startDayTime.toString() + "\n" +
        "  CurrentTask: " + currentTask?.name + "\n" +
        "  TaskGenerated: " + taskGeneratedTime?.toString() +
        "  DelayedTask: " + delayedTask
    }

    fun setNewGeneratedTime(){
        taskGeneratedTime = startDayTime.atDate(LocalDateTime.now().toLocalDate())
    }

    fun setNewRelapseTime(){
        lastDateLapsed = startDayTime.atDate(LocalDateTime.now().toLocalDate())
    }
}
