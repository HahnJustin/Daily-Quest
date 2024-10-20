package com.example.dailyquest.ui.home

import RepeatingNinePatchDrawable
import android.content.Context
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.dailyquest.MainActivity
import com.example.dailyquest.R
import com.example.dailyquest.database.AppDatabase
import com.example.dailyquest.database.Task
import com.example.dailyquest.databinding.FragmentHomeBinding
import com.example.dailyquest.models.DataContainer
import com.example.dailyquest.utils.JsonManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var completeButton : Button? = null
    private var extraActionButton : Button? = null
    private var scrollImage : ImageView? = null
    private var task : Task? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Set Repeatable Drawable Border
        val imageView = root.findViewById<ImageView>(R.id.test_image)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.split_wood_ui)
        val repeatingDrawable = RepeatingNinePatchDrawable(bitmap, 168, 128)
        imageView.background = repeatingDrawable

        val backgroundView = root.findViewById<ImageView>(R.id.background_corners)
        val backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.simple_but_fancy_ui_corners)
        val backgroundDrawable = RepeatingNinePatchDrawable(backgroundBitmap, 100, 28)
        backgroundView.background = backgroundDrawable

        val descView = root.findViewById<TextView>(R.id.quest_desc_label)
        val descBitmap = BitmapFactory.decodeResource(resources, R.drawable.wood_canvas_ui)
        val descDrawable = RepeatingNinePatchDrawable(descBitmap, 85, 85)
        descView.background = descDrawable

        return root
    }

    override fun onStart() {
        super.onStart()

        val jsonManager = JsonManager(requireContext())
        val dataContainer = jsonManager.loadData()
        val task : Task? = dataContainer?.currentTask

        val db = AppDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        //Extra Button
        extraActionButton = binding.root.findViewById(R.id.extra_action_button)
        extraActionButton?.setOnClickListener {
            if (task?.isCompleted == true) {
                showCompletedDialog(dataContainer)
            } else if (task != null) {
                showUncompletedDialog(dataContainer)
            }
            else{
                extraActionButton?.visibility = GONE
            }
        }


        //Streak Related
        val streakText = binding.streakLabel
        val streakImage : ImageView = binding.streakImage
        val streak: Int = (activity as? MainActivity)?.getStreak()!!

        streakText.text = buildString {
            append("Streak: ")
            append(streak)
        }

        val streakResId = getStreakResID(streak)
        if(streakResId >= 0) {
            streakImage.setImageResource(streakResId)
            streakImage.visibility = VISIBLE
        }
        else{
            streakImage.visibility = GONE
        }

        //Task Related
        val nameText = binding.questNameLabel
        val descText = binding.questDescLabel

        scrollImage = binding.scrollImage
        completeButton = binding.completeQuestButton

        // Find the rotating background image
        val rotatingBackground = view?.findViewById<ImageView>(R.id.rotating_background)

        // Create a rotation animation
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 20000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }

        // Start the animation
        rotatingBackground?.startAnimation(rotateAnimation)

        if(task != null) {
            Log.d("HomeFragment", "Current Task is ${task.name}")
            nameText.text = task.name
            descText.text = task.description

            completeButton?.setOnClickListener {
                toggleCompleted(true)

                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.delete(task)
                    task.isCompleted = true
                    task.completedDate = LocalDateTime.now().toString()
                    taskDao.insert(task)

                    dataContainer.currentTask = task
                    jsonManager.saveData(dataContainer)
                }
            }

            if(dataContainer.delayedTask){
                toggleDelayedUI()
            }
            else {
                toggleCompleted(task.isCompleted)
            }
        }
        else{
            Log.d("HomeFragment", "Current Task is null")
            nameText.text = "No Task Today"
            descText.text = "Enjoy your time off!"
            completeButton!!.visibility = View.GONE
            scrollImage?.setImageResource(R.drawable.quick_desert_island3)
        }
    }

    fun toggleCompleted(completed : Boolean){
        if(completed){
            scrollImage?.setImageResource(R.drawable.scroll_closed)
            task?.isCompleted = true
            completeButton?.text = "QUEST COMPLETED!"
            completeButton?.isEnabled = false;
        }
        else{
            scrollImage?.setImageResource(R.drawable.scroll_art)
            completeButton?.text = "IVE COMPLETED IT"
            completeButton?.isEnabled = true;
        }
        completeButton?.visibility = View.VISIBLE
    }

    fun toggleDelayedUI(){
        scrollImage?.setImageResource(R.drawable.scroll_art)
        task?.isCompleted = true
        completeButton?.text = "FAILURE FOR TODAY"
        completeButton?.isEnabled = false;

        completeButton?.visibility = View.VISIBLE
        extraActionButton?.visibility = View.GONE
    }

    fun getStreakResID( streak : Int) : Int {
        when (streak) {
            0 -> {return -1}
            in 1..14 -> {return R.drawable.streak}
            in 15..29 -> {return R.drawable.streak1half}
            in 30..44 -> {return R.drawable.streak2}
            in 45..59 -> {return R.drawable.streak3}
            in 60..119 -> {return R.drawable.streak_mid}
            in 120..179 -> {return R.drawable.streak_mid1half}
            in 180..239 -> {return R.drawable.streak_mid2}
            in 240..299 -> {return R.drawable.streak_mid3}
            in 300..364 -> {return R.drawable.streak_mid4}
            in 365..547-> {return R.drawable.streak_big}
            in 548..729-> {return R.drawable.streak_big1half}
            in 730..912-> {return R.drawable.streak_big2}
            in 913..1094-> {return R.drawable.streak_big3}
            else -> {return R.drawable.streak_big4}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUncompletedDialog(dataContainer: DataContainer) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Have you tried the best physically and mentally you can, but still have failed to accomplish '${dataContainer.currentTask!!.name}'. Would you like to delay this quest?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                delayQuest(dataContainer) // Call the function for delaying the quest
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Quest Uncompleted")
        alert.show()
    }

    private fun showCompletedDialog(dataContainer: DataContainer) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Have you actually not finished '${dataContainer.currentTask!!.name}'?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                handleNotFinishedQuest(dataContainer) // Call the function for handling not finished quest
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Quest Completed")
        alert.show()
    }

    private fun delayQuest(dataContainer : DataContainer) {
        CoroutineScope(Dispatchers.Default).launch {
            val db = AppDatabase.getDatabase(requireContext())
            val taskDao = db.taskDao()
            taskDao.delete(dataContainer.currentTask!!)

            val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val decreaseIntensity = preferences.getInt("priority_decrease_intensity", 10)

            dataContainer.currentTask!!.priority =
                dataContainer.currentTask!!.priority?.plus(250 * decreaseIntensity)
            taskDao.insert(dataContainer.currentTask!!)

            dataContainer.delayedTask = true

            saveDataContainer(dataContainer)
            Log.d("HomeFragment", "Delaying Task: ${dataContainer.currentTask}")
        }

        toggleDelayedUI()
    }

    private fun handleNotFinishedQuest(dataContainer : DataContainer) {
        CoroutineScope(Dispatchers.Default).launch {
            val db = AppDatabase.getDatabase(requireContext())
            val taskDao = db.taskDao()
            taskDao.delete(dataContainer.currentTask!!)

            dataContainer.currentTask!!.isCompleted = false
            taskDao.insert(dataContainer.currentTask!!)

            saveDataContainer(dataContainer)

            Log.d("HomeFragment", "Reverse Completed Task: ${dataContainer.currentTask}")
        }
        toggleCompleted(false)
    }

    private fun saveDataContainer(newDataContainer: DataContainer){
        val jsonManager = JsonManager(requireContext())
        jsonManager.saveData(newDataContainer)
        Log.d("HomeFragment", "Saved: $newDataContainer")
    }
}