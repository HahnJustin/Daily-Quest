package com.example.dailyquest.ui.databaseviewer

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyquest.R
import com.example.dailyquest.database.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_COMPLETED = 1
        private const val VIEW_TYPE_UNCOMPLETED = 2
    }

    // Determine the view type based on whether the task is completed
    override fun getItemViewType(position: Int): Int {
        return if (tasks[position].isCompleted) VIEW_TYPE_COMPLETED else VIEW_TYPE_UNCOMPLETED
    }

    // Define two different ViewHolders for completed and uncompleted tasks
    inner class CompletedTaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.task_name)
        val taskDescription: TextView = view.findViewById(R.id.task_description)
        val completedTimeText: TextView = view.findViewById(R.id.completed_time)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
        val priorityImage: ImageView = view.findViewById(R.id.priority_image)

        fun bind(task: Task) {
            taskName.text = task.name
            taskDescription.text = task.description
            deleteButton.setOnClickListener { onDeleteClick(task) }

            val priorityResId = getPriorityResID(task.priority!!)
            priorityImage.setImageResource(priorityResId)

            if(task.completedDate != null) {
                val time = LocalDateTime.parse(task.completedDate)
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                completedTimeText.text = time.format(dateTimeFormatter)
            }
            else{
                completedTimeText.text = ""
            }

            if(task.description.isNullOrEmpty()){
                taskDescription.visibility = GONE
            }
            else{
                taskDescription.visibility = VISIBLE
            }
        }
    }

    inner class UncompletedTaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.task_name)
        val taskDescription: TextView = view.findViewById(R.id.task_description)
        val editButton: ImageButton = view.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
        val priorityImage: ImageView = view.findViewById(R.id.priority_image)

        fun bind(task: Task) {
            taskName.text = task.name
            taskDescription.text = task.description
            editButton.setOnClickListener { onEditClick(task) }
            deleteButton.setOnClickListener { onDeleteClick(task) }

            val priorityResId = getPriorityResID(task.priority!!)
            priorityImage.setImageResource(priorityResId)

            if(task.description.isNullOrEmpty()){
                taskDescription.visibility = GONE
            }
            else{
                taskDescription.visibility = VISIBLE
            }
        }
    }

    // Inflate the appropriate layout depending on the task's completion status
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COMPLETED) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.completed_task, parent, false)
            CompletedTaskViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task, parent, false)
            UncompletedTaskViewHolder(view)
        }
    }

    // Bind the data to the appropriate ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val task = tasks[position]
        if (holder is CompletedTaskViewHolder) {
            holder.bind(task)
        } else if (holder is UncompletedTaskViewHolder) {
            holder.bind(task)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    fun getPriorityResID( priority : Int) : Int {
        when (priority) {
            0 -> {return R.drawable.urgent_priority_icon}
            in 1..2500 -> {return R.drawable.high_priority_icon}
            in 2501..5000 -> {return R.drawable.normal_priority_icon}
            in 5001..7500 -> {return R.drawable.low_priority_icon}
            in 7501..1000000 -> {return R.drawable.very_low_priority_icon}
            else -> {return R.drawable.empty_x16}
        }
    }
}
