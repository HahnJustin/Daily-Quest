package com.example.dailyquest.ui.addquest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dailyquest.R
import com.example.dailyquest.database.AppDatabase
import com.example.dailyquest.database.Task
import kotlinx.coroutines.launch

class AddQuestFragment : Fragment() {

    private var questNameInput: EditText? = null
    private var questDescInput: EditText? = null
    private var prioritySpinner: Spinner? = null
    private var selectedPriorityValue: Int = 5000  // Default to "normal" priority (5000)
    private var task: Task? = null


    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: Task?): AddQuestFragment {
            val fragment = AddQuestFragment()
            val args = Bundle()
            args.putParcelable(ARG_TASK, task)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_quest, container, false)

        // Access the button and EditTexts
        questNameInput = view.findViewById(R.id.quest_name_input)
        questDescInput = view.findViewById(R.id.quest_desc_input)
        prioritySpinner = view.findViewById(R.id.priority_spinner)
        val addTaskButton: Button = view.findViewById(R.id.add_task_button)

        // Set up the priority spinner
        setupPrioritySpinner()

        // Get the database instance
        val db = AppDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        // Initially disable the "Add Task" button
        addTaskButton.isEnabled = false

        // Add a TextWatcher to the questNameInput field
        questNameInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the button only if the EditText is not empty
                addTaskButton.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })

        // Handle button click
        addTaskButton.setOnClickListener {
            val name = questNameInput?.text.toString()
            val description = questDescInput?.text.toString()

            clear()

            // Handle adding the task or show a message
            Toast.makeText(requireContext(), "Task Added: $name ", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch {
                val task = Task(name = name, description = description, priority = selectedPriorityValue)
                Log.d("AddQuestFragment", "Added task: $task")
                taskDao.insert(task)
            }
        }

        return view
    }

    private fun setupPrioritySpinner() {
        val priorityOptions = arrayOf("Very Low", "Low", "Normal", "High", "Urgent")
        val priorityValues = mapOf(
            "Very Low" to 10000,
            "Low" to 7500,
            "Normal" to 5000,
            "High" to 2500,
            "Urgent" to 0
        )

        // Set up the adapter for the spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorityOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner?.adapter = adapter

        // Set up the listener for the spinner
        prioritySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPriority = parent.getItemAtPosition(position).toString()
                selectedPriorityValue = priorityValues[selectedPriority] ?: 5000  // Default to "Normal" if not found
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Default selection to "Normal" priority
        prioritySpinner?.setSelection(2)  // "Normal" is at index 2
    }

    private fun clear() {
        questNameInput?.text?.clear()
        questDescInput?.text?.clear()
        prioritySpinner?.setSelection(2)  // Reset to "Normal" priority
    }
}
