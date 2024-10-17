package com.example.dailyquest.ui.addquest

import RepeatingNinePatchDrawable
import android.graphics.BitmapFactory
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
import androidx.navigation.fragment.findNavController
import com.example.dailyquest.MainActivity
import com.example.dailyquest.R
import com.example.dailyquest.database.AppDatabase
import com.example.dailyquest.database.Task
import com.example.dailyquest.utils.JsonManager
import kotlinx.coroutines.launch
import kotlin.math.abs

class AddQuestFragment : Fragment() {

    private val priorityOptions = arrayOf("Very Low", "Low", "Normal", "High", "Urgent")
    private val priorityValues = mapOf(
        "Very Low" to 10000,
        "Low" to 7500,
        "Normal" to 5000,
        "High" to 2500,
        "Urgent" to 0
    )

    //private var actionLabel: TextView? = null
    private var questNameInput: EditText? = null
    private var questDescInput: EditText? = null
    private var prioritySpinner: Spinner? = null
    private var addQuestButton: Button? = null
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
            @Suppress("DEPRECATION")
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_quest, container, false)

        val editLayout  = view.findViewById<LinearLayout>(R.id.data_entrance_layout)
        val descBitmap = BitmapFactory.decodeResource(resources, R.drawable.wood_canvas_ui)
        val descDrawable = RepeatingNinePatchDrawable(descBitmap, 85, 85)
        editLayout.background = descDrawable

        // Access the button and EditTexts
        //actionLabel = view.findViewById(R.id.action_label)
        questNameInput = view.findViewById(R.id.quest_name_input)
        questDescInput = view.findViewById(R.id.quest_desc_input)
        prioritySpinner = view.findViewById(R.id.priority_spinner)
        addQuestButton = view.findViewById(R.id.add_task_button)

        // Set up the priority spinner
        setupPrioritySpinner()

        // Sets the task if in edit mode
        setTask()

        // Get the database instance
        val db = AppDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        // Add a TextWatcher to the questNameInput field
        questNameInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Enable the button only if the EditText is not empty
                addQuestButton?.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                // Not used
            }
        })

        // Handle button click
        addQuestButton?.setOnClickListener {
            val name = questNameInput?.text.toString()
            val description = questDescInput?.text.toString()

            //Add task
            if(task == null) {
                clear()
                // Handle adding the task or show a message
                Toast.makeText(requireContext(), "Quest Added: $name ", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    val task = Task(
                        name = name,
                        description = description,
                        priority = selectedPriorityValue
                    )
                    Log.d("AddQuestFragment", "Added task: $task")
                    taskDao.insert(task)
                }
            }
            //Edit task
            else{
                Toast.makeText(requireContext(), "Quest Edited: $name ", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    taskDao.delete(task!!)

                    val editedTask = Task(
                        id = task!!.id,
                        name = name,
                        description = description,
                        priority = selectedPriorityValue
                    )
                    Log.d("AddQuestFragment", "Edited task: $editedTask")
                    taskDao.insert(editedTask)

                    val jsonManager = JsonManager(requireContext())
                    val dataContainer = jsonManager.loadData()

                    val containerTask = dataContainer?.currentTask
                    Log.d("AddQuestFragment", "Current Home task: $task")
                    Log.d("AddQuestFragment", "Current Container task: $containerTask")

                    if(containerTask == task) {  // This compares the content of the tasks
                        dataContainer?.currentTask = editedTask
                        jsonManager.saveData(dataContainer!!)
                        Log.d("AddQuestFragment", "Tasks are the same")
                    }

                    findNavController().popBackStack()
                }
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.hideFab()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.showFab()
    }

    private fun setupPrioritySpinner() {
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
    }

    private fun getPriorityIndex(value : Int?) : Int{
        if(value != null) {
            var closestPriorityStr = ""
            var closestPriorityVal = Int.MAX_VALUE
            for (entry in priorityValues) {
                if (abs(value - entry.value) < abs(value - closestPriorityVal)) {
                    closestPriorityVal = entry.value
                    closestPriorityStr = entry.key
                }
            }
            return priorityOptions.indexOf(closestPriorityStr)
        }
        else{
            return 0
        }
    }

    private fun setTask(){
        addQuestButton?.isEnabled = task != null

        if(task != null){
            questNameInput?.setText(task!!.name)
            questDescInput?.setText(task!!.description)
            prioritySpinner?.setSelection(getPriorityIndex(task!!.priority))  // Reset to "Normal" priority
            addQuestButton?.text = "Edit Quest"
            //actionLabel?.text = "Edit Quest"
        }
        else{
            //actionLabel?.text = "Add Quest"
            addQuestButton?.text = "Add Quest"
            clear()
        }
    }

    private fun clear() {
        questNameInput?.text?.clear()
        questDescInput?.text?.clear()
        prioritySpinner?.setSelection(2)  // Reset to "Normal" priority
    }
}
