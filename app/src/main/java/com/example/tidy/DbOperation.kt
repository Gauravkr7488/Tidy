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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.tidy.constants.RepeatTypes
import com.tidy.sqldelight.BlockedTask
import com.tidy.sqldelight.Task
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class DbOperation(
    private val db: AppDatabase
) {
    suspend fun saveTaskWithId(task: Task) = withContext(Dispatchers.IO) {
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

    suspend fun blockTask(taskId: Long, blockerId: Long) = withContext(Dispatchers.IO) {
        db.taskQueries.blockTask(taskId, blockerId)
    }

    suspend fun getBlockedTasks(taskId: Long) = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getBlockedTasks(taskId).executeAsList()
    }

    suspend fun getBlockedByTasks(taskId: Long) = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getBlockedByTasks(taskId).executeAsList()
    }

    suspend fun getAllBlockers(): List<BlockedTask> =
        withContext(Dispatchers.IO) {
            return@withContext db.taskQueries.getAllBlockers().executeAsList()
        }

    open suspend fun saveTask(task: Task) = withContext(Dispatchers.IO) {
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
        ).value
    }

    suspend fun getLastRowInsertId() = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getLastRowInsertId().executeAsOne()
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
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
    }

    open suspend fun getTask(id: Long): Task = withContext(Dispatchers.IO) {
        db.taskQueries.getTaskById(id).executeAsOne()
    }

    suspend fun taskGetAll(): List<Task> = withContext(Dispatchers.IO) {
        db.taskQueries.getAll().executeAsList()
    }

    open suspend fun deleteTask(id: Long): Unit = withContext(Dispatchers.IO) {
        db.taskQueries.deleteTask(id)
    }

    suspend fun taskDeleteALl() = withContext(Dispatchers.IO) {
        db.taskQueries.deleteAllTasks()
    }

    fun observeTasks(): Flow<List<Task>> =
        db.taskQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)

    suspend fun updateParentsDoneStatus(parentId: Long?) = withContext(Dispatchers.IO) {
        db.transaction {
            var currentId = parentId
            while (currentId != null) {
                val allChildrenDone = db.taskQueries.areAllChildrenDone(currentId).executeAsOne()
                db.taskQueries.updateDoneStatus(
                    id = currentId,
                    done = allChildrenDone
                )
                currentId = db.taskQueries.getParentId(currentId).executeAsOne().parentId
            }
        }
    }

    suspend fun updateTaskAndDescendantsHideStatus(taskId: Long, hide: Boolean) =
        withContext(Dispatchers.IO) {
            db.taskQueries.updateTaskAndDescendantsHideStatus(taskId = taskId, hide = hide)
        }

    suspend fun resetSkippedTasks() = withContext(Dispatchers.IO) {
        db.taskQueries.resetSkippedTasks().value
    }

    suspend fun getNonDoneDailyTasksWithDueDate() = withContext(Dispatchers.IO) {
        return@withContext db.taskQueries.getNonDoneDailyTasksWithDueDate(RepeatTypes.DAY).executeAsList()
    }

    suspend fun deleteAllBlocks(taskId: Long) = withContext(Dispatchers.IO){
        db.taskQueries.removeAllBlocks(taskId)
    }
}