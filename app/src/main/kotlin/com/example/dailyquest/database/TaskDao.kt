package com.example.dailyquest.database

import androidx.room.*

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getUncompletedTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTasks(): List<Task>
}