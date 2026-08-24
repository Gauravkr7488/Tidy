package com.example.tidy

import com.example.tidy.constants.RepeatTypes
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskService(
    db: AppDatabase,
    private val scheduleService: ScheduleService
) : DbOperation(db = db) {
    override suspend fun saveTask(task: Task): Long {
        if (task.parentId != null) {
            val descendants = getAllDescendants(task.id)
            val isLooping = task.id == task.parentId || descendants.contains(task.parentId)
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
            if (task.repeatType != RepeatTypes.NONE) scheduleService.rescheduleTask(task)
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
            updateTask(it.copy(hide = true, done = true))
            scheduleService.scheduleTask(it)
        }
    }

    suspend fun updateTasks(tasks: List<Task>) = withContext(Dispatchers.IO) {
        db.transaction {
            tasks.forEach { task ->
                tq.updateTask(
                    id = task.id,
                    title = task.title,
                    done = task.done,
                    repeatType = task.repeatType,
                    repeatDays = task.repeatDays,
                    description = task.description,
                    hide = task.hide,
                    parentId = task.parentId,
                    blockStatus = task.blockStatus,
                    priority = task.priority,
                    dueDateAndTime = task.dueDateAndTime,
                    frequencyNumber = task.frequencyNumber,
                    endDate = task.endDate,
                    skipStatus = task.skipStatus,
                    repeatAfterDone = task.repeatAfterDone
                )
            }
        }
    }

    suspend fun deleteTasks(tasks: List<Task>) = withContext(Dispatchers.IO) {
        db.transaction {
            tasks.forEach {
                tq.deleteTask(it.id)
            }
        }
    }
}