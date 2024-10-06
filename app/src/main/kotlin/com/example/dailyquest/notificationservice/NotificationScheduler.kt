package com.example.dailyquest.notificationservice

import com.example.dailyquest.NotificationReceiver
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.dailyquest.MainActivity
import com.example.dailyquest.R
import java.util.Calendar

 class NotificationScheduler {
     fun scheduleDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the time for the alarm (7:00 AM)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 55)
            set(Calendar.SECOND, 0)
        }

        // If the time is in the past, set it for the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

     fun sendTestNotification(context: Context) {
         val notificationManager =
             context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

         val notificationIntent = Intent(context, MainActivity::class.java)
         val pendingIntent = PendingIntent.getActivity(
             context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
         )

         val notification = NotificationCompat.Builder(context, "daily_reminder")
             .setContentTitle("Test Notification")
             .setContentText("This is a test notification to verify the system works.")
             .setSmallIcon(R.drawable.scroll_icon)
             .setContentIntent(pendingIntent)
             .setAutoCancel(true)
             .build()

         notificationManager.notify(1002, notification)
     }
}