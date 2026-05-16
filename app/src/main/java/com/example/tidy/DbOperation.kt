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
import com.yourapp.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.tidy.sqldelight.Task
import kotlinx.coroutines.flow.Flow

class DbOperation(
    private val db: AppDatabase
) {
    suspend fun saveNewTaskList(list: List<Task>) = withContext(Dispatchers.IO) {
        list.forEach { task ->
            db.taskQueries.saveNewTask(
                id = task.id,
                title = task.title,
                done = task.done,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays,
                description = task.description,
                hide = task.hide,
                createdAt = task.createdAt,
                parentId = task.parentId
            )
        }
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
                parentId = task.parentId
            )
            val id: Long? = db.taskQueries.getLastId().executeAsOneOrNull()
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
                parentId = task.parentId
            )
            return@withContext task.id
        }
    }

    suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) {
        db.taskQueries.getTaskById(id).executeAsOneOrNull()
    }

    suspend fun taskGetAll(): List<Task> = withContext(Dispatchers.IO) {
        db.taskQueries.getAll().executeAsList()
    }


    suspend fun updateChildrenRepeatStatus(parentId: Long): Unit = withContext(
        Dispatchers.IO
    ) {
        val task = getTask(parentId) ?: return@withContext
        val taskChildren = db.taskQueries.getChildren(task.id).executeAsList()
        taskChildren.forEach { child ->
            val freshChild = getTask(child.id) ?: return@withContext
            val newTask =
                freshChild.copy(repeatType = task.repeatType, repeatDays = task.repeatDays)
            saveTask(newTask)
            updateChildrenRepeatStatus(freshChild.id)
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

    suspend fun skipTask(id: Long) = withContext(Dispatchers.IO) {
        val task = getTask(id) ?: return@withContext
        saveTask(
            task.copy(hide = 1L)
        )
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        db.taskQueries.deleteTask(id)
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

    suspend fun getLastResetDate() = withContext(Dispatchers.IO) {
        db.lastResetQueries.getLastReset().executeAsOneOrNull()
    }

    suspend fun setLastResetToday(todayDate: String) = withContext(Dispatchers.IO) {
        db.lastResetQueries.setLastReset(todayDate)
    }

    fun observeTasks(): Flow<List<Task>> =
        db.taskQueries.getAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
}