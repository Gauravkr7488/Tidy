/*
 * Copyright (C) 2026  Gaurav Kumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.example.tidy

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.TaskActions
import com.example.tidy.constants.WeekDays
import com.tidy.sqldelight.BlockedTask
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tidy.sqldelight.Task
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class DbOperation(
    private val db: AppDatabase, private val context: Context
) { // todo: here only db queries should be executed all the logical stuff should go to the viewmodel
    suspend fun saveNewTaskList(list: List<Task>) = withContext(Dispatchers.IO) {
        list.forEach { task ->
            db.taskQueries.saveTaskWithId(
                id = task.id,
                title = task.title,
                done = task.done,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays,
                description = task.description,
                hide = task.hide,
                createdAt = task.createdAt,
                parentId = task.parentId,
                blockStatus = task.blockStatus,
                priority = task.priority,
                dueDateAndTime = task.dueDateAndTime,
                frequencyNumber = task.frequencyNumber,
                startDate = task.startDate,
                endDate = task.endDate,
                time = task.time
            )
        }
    }

    suspend fun addBlocker(taskId: Long, blockerId: Long) = withContext(Dispatchers.IO) {
        db.taskQueries.blockTask(taskId, blockerId)
    }

    suspend fun getBlockedTasks(taskId: Long) = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getBlockedTasks(taskId).executeAsList()
    }

    suspend fun getBlockedTask(taskId: Long, blockerId: Long): BlockedTask? =
        withContext(Dispatchers.IO) {
            return@withContext db.taskQueries.getBlockTask(taskId, blockerId).executeAsOneOrNull()
        }


    suspend fun getBlockedByTasks(taskId: Long) = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getBlockedByTasks(taskId).executeAsList()
    }

    suspend fun getAllBlockers(): List<BlockedTask> = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getAllBlockers().executeAsList()
    }

    suspend fun isChildAncestorOfParent(parentId: Long, childId: Long): Boolean =
        withContext(Dispatchers.IO) { // Guard against loops
            if (parentId == childId) return@withContext true
            val grandParentId = getTask(parentId)?.parentId ?: return@withContext false
            return@withContext isChildAncestorOfParent(grandParentId, childId)
        }

    suspend fun saveTask(task: Task): Long? = withContext(Dispatchers.IO) {
        if (task.parentId != null && isChildAncestorOfParent(
                task.parentId,
                task.id
            )
        ) return@withContext null // todo should return some kind of error or warning
        if (task.id == 0L) {
            db.taskQueries.saveTask(
                title = task.title,
                done = task.done,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays,
                description = task.description,
                hide = task.hide,
                createdAt = task.createdAt,
                parentId = task.parentId,
                blockStatus = task.blockStatus,
                priority = task.priority,
                dueDateAndTime = task.dueDateAndTime,
                frequencyNumber = task.frequencyNumber,
                startDate = task.startDate,
                endDate = task.endDate,
                time = task.time
            )
            val id: Long? = db.taskQueries.getLastId().executeAsOneOrNull()
            if (task.dueDateAndTime != null && id != null) Utils.scheduleWork(
                context,
                taskId = id,
                scheduleTime = task.dueDateAndTime,
                action = TaskActions.UPDATE_PRIORITY
            )
            if (task.frequencyNumber != null && id != null) {
                val scheduleTime: Long = getScheduleTime(task.frequencyNumber, task)
                if (task.endDate == null || task.endDate > scheduleTime) {
                    Utils.scheduleWork(
                        context = context,
                        taskId = id,
                        scheduleTime = scheduleTime,
                        action = TaskActions.UNARCHIVE
                    )
                }
            }
            return@withContext id

        } else {
            db.taskQueries.updateTask(
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
                startDate = task.startDate,
                endDate = task.endDate,
                time = task.time
            )
            if (task.dueDateAndTime != null) {
                Utils.cancelDueDateWork(
                    context, task.id,
                    action = TaskActions.UPDATE_PRIORITY
                )
                Utils.scheduleWork(
                    context,
                    taskId = task.id,
                    scheduleTime = task.dueDateAndTime,
                    action = TaskActions.UPDATE_PRIORITY
                )
            }
            if (task.frequencyNumber != null) {
                Utils.cancelDueDateWork(
                    context, task.id,
                    action = TaskActions.UNARCHIVE
                )
                val scheduleTime = getScheduleTime(task.frequencyNumber, task)
                Utils.scheduleWork(
                    context,
                    taskId = task.id,
                    scheduleTime = scheduleTime,
                    action = TaskActions.UNARCHIVE
                )
            }
            return@withContext task.id
        }
    }

    suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) {
        db.taskQueries.getTaskById(id).executeAsOneOrNull()
    }

    suspend fun taskGetAll(): List<Task> = withContext(Dispatchers.IO) {
        db.taskQueries.getAll().executeAsList()
    }


    suspend fun updateChildrenRepeatAndHideStatus(parentId: Long): Unit =
        withContext( // update the status of children to match the parent
            Dispatchers.IO
        ) {
            val task = getTask(parentId) ?: return@withContext
            val taskChildren = db.taskQueries.getChildren(task.id).executeAsList()
            taskChildren.forEach { child ->
                val freshChild = getTask(child.id) ?: return@withContext
                val newTask =
                    freshChild.copy(
                        repeatType = task.repeatType,
                        repeatDays = task.repeatDays,
                        hide = task.hide
                    )
                saveTask(newTask)
                updateChildrenRepeatAndHideStatus(freshChild.id)
            }
        }

    suspend fun updateDoneStatus(id: Long) = withContext(Dispatchers.IO) {

        val task = getTask(id) ?: return@withContext
        saveTask(
            task.copy(
                done = if (task.done == 1L) 0L else 1L
            )
        )
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        db.taskQueries.deleteTask(id)
        Utils.cancelAllWork(
            context = context, taskId = id
        )
    }

    suspend fun getChildren(id: Long) = withContext(Dispatchers.IO) {
        db.taskQueries.getChildren(id).executeAsList()
    }

    suspend fun updateParentDoneStatus(parentId: Long): Unit = withContext(Dispatchers.IO) {
        val freshParent = getTask(parentId) ?: return@withContext
        val children = getChildren(freshParent.id)
        val allChildrenDone = children.all { it.done == 1L }
        saveTask(
            freshParent.copy(
                done = if (allChildrenDone) 1L else 0L
            )
        )
        val grandParentId = freshParent.parentId ?: return@withContext
        updateParentDoneStatus(grandParentId)

    }

    suspend fun taskDeleteALl() = withContext(Dispatchers.IO) {
        db.taskQueries.deleteAllTasks()
    }

    suspend fun taskSaveList(tasks: List<Task>) = withContext(Dispatchers.IO) {
        tasks.forEach { saveTask(it) }
    }

    suspend fun getLastResetDate(): String? = withContext(Dispatchers.IO) {
        db.lastResetQueries.getLastReset().executeAsOneOrNull()
    }

    suspend fun setLastResetToday(todayDate: String): Unit = withContext(Dispatchers.IO) {
        db.lastResetQueries.setLastReset(todayDate)
    }

    fun observeTasks(): Flow<List<Task>> =
        db.taskQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
}

private fun getScheduleTime(
    frequencyNumber: String,
    task: Task
): Long {
    var scheduleTime: Long
    val duration: Long = when (task.repeatType) {
        RepeatTypes.MINUTE -> frequencyNumber.toInt().minutes.inWholeMilliseconds
        RepeatTypes.HOUR -> frequencyNumber.toInt().hours.inWholeMilliseconds
        RepeatTypes.DAY -> frequencyNumber.toInt().days.inWholeMilliseconds
        RepeatTypes.WEEK -> {
            val list = task.repeatDays.split(",")
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val x = list.map {
                val day = when (it) {
                    WeekDays.SUN -> Calendar.SUNDAY
                    WeekDays.MON -> Calendar.MONDAY
                    WeekDays.TUE -> Calendar.TUESDAY
                    WeekDays.WED -> Calendar.WEDNESDAY
                    WeekDays.THU -> Calendar.THURSDAY
                    WeekDays.FRI -> Calendar.FRIDAY
                    WeekDays.SAT -> Calendar.SATURDAY
                    else -> 0
                }
                var remainingDays = day - today
                if (remainingDays <= 0) remainingDays += 7
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, remainingDays)
                c.timeInMillis
            }
            x.min()
        }

        RepeatTypes.MONTH -> {
            val list = task.repeatDays.split(",").map { it.toLong() }
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val next = list.filter { it <= today }.min()
            val interval = next - today
            interval.toInt().days.inWholeMilliseconds
        }
        RepeatTypes.YEAR -> 0

        else -> 0
    }
    scheduleTime = System.currentTimeMillis() + duration
    if (task.startDate != null) {
        while (task.startDate > scheduleTime) scheduleTime += duration
    }
    return scheduleTime
}