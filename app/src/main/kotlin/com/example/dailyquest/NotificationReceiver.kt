package com.example.dailyquest

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.dailyquest.database.AppDatabase
import com.example.dailyquest.models.DataContainer
import com.example.dailyquest.utils.JsonManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Triggered with intent: $intent.action")

        var dataContainer = loadDataContainer(context)
        if (dataContainer == null) {
            dataContainer = DataContainer()
            Log.d("NotificationReceiver", "Data Container was null")
        }
        else{
            Log.d("NotificationReceiver", "Loaded: $dataContainer")
        }

        scheduleTaskNotification(context, dataContainer)
    }

    private fun loadDataContainer(context: Context): DataContainer? {
        val jsonManager = JsonManager(context)
        return jsonManager.loadData()
    }

    fun scheduleTaskNotification(context: Context, dataContainer: DataContainer) {
        // Get the taskDao
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()

        // Get the next notification time (startDayTime 24 hours after taskGeneratedTime)
        var nextNotificationTime = dataContainer.startDayTime.atDate(LocalDate.now().minusDays(1));
        if(dataContainer.taskGeneratedTime != null) {
            nextNotificationTime = LocalDateTime.of(
                //TODO: revert back to this, this is temporary for testing
                // dataContainer.taskGeneratedTime!!.plusDays(1).toLocalDate(),
                LocalDateTime.now().toLocalDate(),
                dataContainer.startDayTime
            )
        }

        // Get the current time
        val currentTime = LocalDateTime.now()

        // If we are past the notification time, send the notification immediately
        if (currentTime.isAfter(nextNotificationTime)) {
            doAsync(CoroutineScope(Dispatchers.Default)){
                sendTaskNotification(context, dataContainer)
            }
        }
        else {
            // Schedule a notification for the calculated time
            scheduleExactNotification(context, nextNotificationTime)
        }

    }

    suspend fun sendTaskNotification(context: Context, dataContainer: DataContainer) {
        Log.d("NotificationReceiver", "Creating and notifying about new task")

        // Get the taskDao and grab the new task from the database
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()
        val tasks =  taskDao.getUncompletedTasks()

        //No Tasks
        if(tasks.isEmpty()) return

        val newTask = tasks.get(0)

        // Send notification about the new task
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "new_quest_alerts")
            .setContentTitle("New Task")
            .setContentText("You have a new task: ${newTask.name}")
            .setSmallIcon(R.drawable.scroll_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1003, notification)

        // Update the DataContainer for the new task
        val newDataContainer = dataContainer.copy(
            startDayTime = dataContainer.startDayTime,
            currentTask = newTask,
            taskGeneratedTime = null,
            lastDateLapsed = dataContainer.lastDateLapsed
        )

        newDataContainer.setNewGeneratedTime()

        //Define if the task was completed, and streak should be reset
        if(dataContainer.currentTask != null && !dataContainer.currentTask!!.isCompleted){
            newDataContainer.setNewRelapseTime()
            Log.d("NotificationReceiver", "Reset Relapse Time")
        }

        // Save new DataContainer
        val jsonManager = JsonManager(context)
        jsonManager.saveData(newDataContainer)
        Log.d("NotificationReceiver", "Saved: $newDataContainer")
    }

    fun scheduleExactNotification(context: Context, notificationTime: LocalDateTime) {
        Log.d("NotificationReceiver", "Scheduling exact notification at: $notificationTime")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Show a dialog prompting the user to allow exact alarms
            doAsync(CoroutineScope(Dispatchers.Default)) {
                showPermissionNotification(context)
            }
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel old alarms
        try {
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("NotificationManager", "AlarmManager update was not canceled. $e")
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        //Set new alarm
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("NotificationManager", "AlarmManager alarm was not set. $e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun showPermissionNotification(context: Context) {
        Log.d("NotificationReceiver", "Showing Notification Request to Schedule Exact Alarm")
        val notificationIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, "new_quest_alerts")
            .setSmallIcon(R.drawable.scroll_icon)
            .setContentTitle("Permission Required")
            .setContentText("Please allow this app to schedule exact alarms.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notificationBuilder.build())
    }


    fun BroadcastReceiver.doAsync(
        appScope: CoroutineScope,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ){
        val pendingResult = goAsync()
        appScope.launch(coroutineContext) { block() }.invokeOnCompletion { pendingResult?.finish() }
    }
}
