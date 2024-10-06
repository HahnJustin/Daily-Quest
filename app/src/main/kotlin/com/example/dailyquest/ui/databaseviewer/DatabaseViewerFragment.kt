package com.example.dailyquest.ui.databaseviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailyquest.database.AppDatabase
import com.example.dailyquest.databinding.FragmentDatabaseViewerBinding
import com.example.dailyquest.database.Task
import com.example.dailyquest.utils.JsonManager
import kotlinx.coroutines.launch

class DatabaseViewerFragment : Fragment() {

    private var _binding: FragmentDatabaseViewerBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskSwitch: SwitchCompat
    private var showCompletedTasks: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatabaseViewerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        taskSwitch = binding.switchTasks

        // Setup the switch to toggle between completed and uncompleted tasks
        taskSwitch.setOnCheckedChangeListener { _, isChecked ->
            showCompletedTasks = isChecked
            setupRecyclerView()
        }

        setupRecyclerView()

        return root
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerViewTasks
        val emptyTextView = binding.emptyTextView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val db = AppDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        // Fetch tasks based on switch state (completed or uncompleted)
        lifecycleScope.launch {
            val tasks = if (showCompletedTasks) {
                taskDao.getCompletedTasks()
            } else {
                taskDao.getUncompletedTasks()
            }

            if (tasks.isEmpty()) {
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                val adapter = TaskAdapter(tasks, ::onEditTask, ::onDeleteTask)
                recyclerView.adapter = adapter
            }
        }
    }

    private fun onEditTask(task: Task) {
        // Handle task editing, you could navigate to an edit screen
        // For now, let's just print it
        // Example: Edit task logic can be implemented here
    }

    private fun onDeleteTask(task: Task) {
        // Show a confirmation dialog before deleting the task
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Quest")
            .setMessage("Are you sure you want to delete \"${task.name}\"?")
            .setPositiveButton("Yes") { dialog, _ ->
                // User confirmed deletion, proceed with task deletion
                deleteTask(task)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                // User canceled, just dismiss the dialog
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteTask(task: Task) {
        val db = AppDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        //Delete task from save data if its active
        val jsonManager = JsonManager(requireContext())
        val dataContainer = jsonManager.loadData()
        if(dataContainer?.currentTask == task) {
            dataContainer.currentTask = null
            jsonManager.saveData(dataContainer)
        }

        // Perform the actual task deletion and refresh the list
        lifecycleScope.launch {
            taskDao.delete(task)
            setupRecyclerView() // Refresh the RecyclerView after deletion
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
