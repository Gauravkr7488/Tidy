package com.example.tidy

import android.content.Context
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase

class TaskService(
    db: AppDatabase,
    private val context: Context,
    private val scheduleService: ScheduleService
) : DbOperation(db = db) {
    override suspend fun saveTask(task: Task): Long {
        if (task.id == 0L) {
            super.saveTask(task)
            val id = getLastRowInsertId() ?: throw Exception("Failed to save new task")
            scheduleService.scheduleTask(task.copy(id = id))
            return id
        } else {
            super.updateTask(task)
            Utils.cancelAlarm(context, task.id)
            if (task.repeatAfterDone == 1L && task.done == 0L) return task.id
            scheduleService.scheduleTask(task)
            return task.id
        }
    }

    override suspend fun deleteTask(id: Long) {
        super.deleteTask(id)
        Utils.cancelAlarm(
            context = context, taskId = id
        )
    }
}