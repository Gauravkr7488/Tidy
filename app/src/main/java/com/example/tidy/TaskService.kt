package com.example.tidy

import android.util.Log
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase

class TaskService(
    db: AppDatabase,
    private val scheduleService: ScheduleService
) : DbOperation(db = db) {
    override suspend fun saveTask(task: Task): Long {
        if (task.parentId != null){
            val descendants = getAllDescendants(task.id)
            val isLooping = descendants.contains(task.parentId)
            if (isLooping) throw Exception("isLooping")
        }
        if (task.id == 0L) {
            super.saveTask(task)
            val id = getLastRowInsertId()
            scheduleService.scheduleTask(task.copy(id = id))
            return id
        } else {
            super.updateTask(task)
            if (task.repeatAfterDone && !task.done) return task.id
            scheduleService.rescheduleTask(task)
            return task.id
        }
    }

    override suspend fun deleteTask(id: Long) {
        super.deleteTask(id)
        scheduleService.cancelSchedule(taskId = id)
    }

    override suspend fun getTask(id: Long): Task {
        return super.getTask(id)
    }

    suspend fun archiveAndRescheduleNonDoneDailyTasksWithDueTime() {
        val tasks = getNonDoneDailyTasksWithDueDate()
        tasks.forEach {
            updateTask(it.copy(hide = true))
            scheduleService.scheduleTask(it)
        }
    }
}