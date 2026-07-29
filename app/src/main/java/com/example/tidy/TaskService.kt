package com.example.tidy

import androidx.compose.runtime.collectAsState
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class TaskService(
    db: AppDatabase,
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
            if (task.repeatAfterDone == 1L && task.done == 0L) return task.id
            scheduleService.rescheduleTask(task)
            return task.id
        }
    }

    override suspend fun deleteTask(id: Long) {
        super.deleteTask(id)
        scheduleService.cancelSchedule(taskId = id)
    }

    suspend fun updateParentsDoneStatus(task: Task) {
        if (task.parentId == null) return
        val tasks = observeTasks().first()
        val freshParent = getTask(task.parentId)
        val children = tasks.filter { it.parentId == freshParent.id }
        val allChildrenDone = children.all { it.done == 1L }
        updateTask(
            freshParent.copy(
                done = if (allChildrenDone) 1L else 0L
            )
        )
        updateParentsDoneStatus(freshParent)
    }

    override suspend fun getTask(id: Long): Task {
        return super.getTask(id) ?: throw Exception("Failed to fetch task")
    }
}