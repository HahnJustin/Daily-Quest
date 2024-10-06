package com.example.dailyquest.ui.databaseviewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailyquest.R
import com.example.dailyquest.database.Task

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.taskName)
        val taskDescription: TextView = view.findViewById(R.id.taskDescription)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name
        holder.taskDescription.text = task.description

        holder.editButton.setOnClickListener { onEditClick(task) }
        holder.deleteButton.setOnClickListener { onDeleteClick(task) }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}
