package com.example.dailyquest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
    private var currentData : DataContainer? = null
    private var streak: Int = 0

    private val _notificationReceiver: BroadcastReceiver = NotificationReceiver()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Load and Maintain Json Data
        jsonDataMaintenance(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        binding.appBarMain.fab.setOnClickListener {
            // view ->
            //Snackbar.make(view, "GAMER GAMER GAMER", Snackbar.LENGTH_LONG)
            //    .setAction("Action", null)
            //    .setAnchorView(R.id.fab).show()

            binding.appBarMain.fab.hide()

            // Navigate to the add quest fragment
            navController.navigate(R.id.nav_add_quest)

        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_settings, R.id.nav_data_viewer
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun showFab() {
        binding.appBarMain.fab.show()
    }

    fun hideFab() {
        binding.appBarMain.fab.hide()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun jsonDataMaintenance(context: Context){
        jsonManager = JsonManager(context)

        currentData = jsonManager.loadData()

        var relapseTime : LocalDateTime
        if (currentData != null) {
            relapseTime = currentData!!.lastDateLapsed;
            println("Last Date Lapsed: ${relapseTime}")
        }
        else{
            relapseTime = LocalDateTime.now()
            val dataContainer = DataContainer()
            jsonManager.saveData(dataContainer)
        }

        streak = Period.between(relapseTime.toLocalDate(), LocalDate.now()).days
    }

    fun getStreak(): Int {
        return streak
    }

    fun onLinkClick(view: View){
        if(view is TextView) {
            var url = view.text
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }

            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
            startActivity(browserIntent)
        }
    }
}