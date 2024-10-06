package com.example.dailyquest.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: String? = null,
    val priority: Int? = null,
    var isCompleted: Boolean = false,
    var completedDate: String? = null
){
    override fun toString() : String{
        return "[DataContainer]: $name \n" +
                "  Description: $description.toString() \n" +
                "  DueDate: $dueDate \n" +
                "  Priority: $priority \n" +
                "  IsCompleted: $completedDate + \n" +
                "  CompletedDate: $completedDate \n"
    }
}
