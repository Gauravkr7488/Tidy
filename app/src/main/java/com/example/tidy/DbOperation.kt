package com.example.tidy

import io.objectbox.Box

class DbOperation(
    private val taskBox: Box<Task>,
    private val lastBoxReset: Box<LastReset>,
) {
    fun saveTask(task: Task): Long {
        return taskBox.put(task)
    }

    fun getTask(id: Long): Task {
        val task = taskBox.get(id)
        return task
    }

    fun updateTask(task: Task, id: Long): Long {
        val oldTask = getTask(id)
        task.id = oldTask.id
        return saveTask(task)
    }

}