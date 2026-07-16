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
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.TaskActions
import com.example.tidy.constants.WeekDays
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

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
                endDate = task.endDate,
                repeatAfterDone = task.repeatAfterDone,
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

    suspend fun saveTask(task: Task): Long? =
        withContext(Dispatchers.IO) {
            if (task.parentId != null && isChildAncestorOfParent(
                    task.parentId,
                    task.id
                )
            ) {
                println("Save Task Failed")
                return@withContext null
            }
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
                    endDate = task.endDate,
                    repeatAfterDone = task.repeatAfterDone,
                )
                val id: Long =
                    db.taskQueries.getLastId().executeAsOneOrNull() ?: return@withContext null
                if (task.repeatType == RepeatTypes.NONE && task.dueDateAndTime == null) return@withContext id
                if (scheduleTask(task, id)) return@withContext null
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
                    endDate = task.endDate,
                    repeatAfterDone = task.repeatAfterDone,
                )
                Utils.cancelAlarm(context, task.id)
                if (task.repeatAfterDone == 1L && task.done == 0L) return@withContext task.id
                scheduleTask(task, task.id)
                return@withContext task.id
            }
        }

    private fun scheduleTask(task: Task, id: Long): Boolean {
        val scheduleDate: Long? = if (task.repeatType != RepeatTypes.NONE) {
            val t = getScheduleDate(
                frequencyNumber = task.frequencyNumber?.toInt() ?: 1,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays.split(",")
            )
            var k = ""
            if (t != null) k = Utils.changeDateFormat(t, "ddMMyy hh mm")
            println("schedule = $k")
            if (task.repeatType == RepeatTypes.MINUTE || task.repeatType == RepeatTypes.HOUR) t else
                Utils.combineDateAndTimeMillis(t, task.dueDateAndTime)
        } else {
            task.dueDateAndTime
        }
        if (scheduleDate == null) return true
        if (task.endDate == null || task.endDate > scheduleDate) {
            Utils.scheduleAlarm(
                context = context,
                taskId = id,
                scheduleTime = scheduleDate,
                action = TaskActions.UNARCHIVE
            )
        }
        return false
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
        Utils.cancelAlarm(
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

private fun getScheduleDate(
    frequencyNumber: Int,
    repeatType: String,
    repeatDays: List<String>,
): Long? {
    val c = Calendar.getInstance()
    c.set(Calendar.SECOND, 0)
    val scheduleTime = when (repeatType) {
        RepeatTypes.MINUTE -> {
            c.add(Calendar.MINUTE, frequencyNumber)
            c.timeInMillis
        }

        RepeatTypes.HOUR -> {
            c.set(Calendar.MINUTE, 0)
            c.add(Calendar.HOUR, frequencyNumber)
            c.timeInMillis
        }

        RepeatTypes.DAY -> {
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.HOUR, 0)
            c.add(Calendar.DAY_OF_YEAR, frequencyNumber)
            c.timeInMillis
        }

        RepeatTypes.WEEK -> {
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.HOUR, 0)
            val today = c.get(Calendar.DAY_OF_WEEK)
            val listScheduleDays: List<Int> = repeatDays.mapNotNull {
                getWeekDayNum(it)
            }
            if (listScheduleDays.any { it > today }) {
                c.set(Calendar.DAY_OF_WEEK, listScheduleDays.first { it > today })
            } else {
                c.set(Calendar.DAY_OF_WEEK, listScheduleDays.first())
                c.add(Calendar.WEEK_OF_YEAR, frequencyNumber)
            }
            c.timeInMillis
        }

        RepeatTypes.MONTH -> {
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.HOUR, 0)
            val today = c.get(Calendar.DAY_OF_MONTH)
            val listScheduleDays = repeatDays.map { it.toInt() }
            if (listScheduleDays.any { it > today }) {
                c.set(Calendar.DAY_OF_MONTH, listScheduleDays.first { it > today })
            } else {
                c.set(Calendar.DAY_OF_MONTH, listScheduleDays.first())
                c.add(Calendar.MONTH, frequencyNumber)
            }
            c.timeInMillis
        }

        RepeatTypes.YEAR -> {
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.HOUR, 0)
            val today = c.timeInMillis
            val listScheduleDays = repeatDays.map { it.toLong() }
            if (listScheduleDays.any { it > today }) {
                val next = listScheduleDays.first { it > today }
                c.timeInMillis = next
            } else {
                c.add(Calendar.YEAR, frequencyNumber)
            }
            c.timeInMillis
        }

        else -> null
    }
    return scheduleTime
}

private fun getWeekDayNum(string: String): Int? = when (string) {
    WeekDays.SUN -> Calendar.SUNDAY
    WeekDays.MON -> Calendar.MONDAY
    WeekDays.TUE -> Calendar.TUESDAY
    WeekDays.WED -> Calendar.WEDNESDAY
    WeekDays.THU -> Calendar.THURSDAY
    WeekDays.FRI -> Calendar.FRIDAY
    WeekDays.SAT -> Calendar.SATURDAY
    else -> null
}