package com.example.dailyquest

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dailyquest.database.Task
import com.example.dailyquest.databinding.ActivityMainBinding
import com.example.dailyquest.models.DataContainer
import com.example.dailyquest.notificationservice.NotificationScheduler
import com.example.dailyquest.utils.JsonManager
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var jsonManager: JsonManager
    private var currentData: DataContainer? = null
    private var streak: Int = 0

    private val _notificationReceiver: BroadcastReceiver = NotificationReceiver()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions at the start
        requestNotificationAndAlarmPermissions()

        // Load and maintain JSON data
        jsonDataMaintenance(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        binding.appBarMain.fab.setOnClickListener {
            binding.appBarMain.fab.hide()

            // Navigate to the add quest fragment
            navController.navigate(R.id.nav_add_quest)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_settings, R.id.nav_data_viewer
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // Function to request notification and exact alarm permissions
    private fun requestNotificationAndAlarmPermissions() {
        val notificationManager = NotificationManagerCompat.from(this)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if notification permission is granted
        if (!notificationManager.areNotificationsEnabled()) {
            showPermissionDialog(
                "Notification Permission",
                "This app needs notification permission to send you task reminders. It won't work without it",
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            )
        }

        // Check if exact alarm permission is required and granted (for API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionDialog(
                    "Exact Alarm Permission",
                    "This app needs exact alarm permission to schedule task reminders. It won't work without it",
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                )
            }
        }
    }

    // Helper function to show a permission dialog
    private fun showPermissionDialog(title: String, message: String, intent: Intent) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Allow") { dialog, _ ->
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun jsonDataMaintenance(context: Context) {
        jsonManager = JsonManager(context)
        currentData = jsonManager.loadData()

        var relapseTime: LocalDateTime
        if (currentData != null) {
            relapseTime = currentData!!.lastDateLapsed
            println("Last Date Lapsed: $relapseTime")
        } else {
            relapseTime = LocalDateTime.now()
            val dataContainer = DataContainer()
            jsonManager.saveData(dataContainer)
        }

        streak = Period.between(relapseTime.toLocalDate(), LocalDate.now()).days
    }

    fun getStreak(): Int {
        return streak
    }

    fun onLinkClick(view: View) {
        if (view is TextView) {
            var url = view.text
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
            startActivity(browserIntent)
        }
    }

    fun showFab() {
        binding.appBarMain.fab.show()
    }

    fun hideFab() {
        binding.appBarMain.fab.hide()
    }
}
