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

import com.example.tidy.Utils.getCurrentDate
import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbOperation(
    private val taskBox: Box<Task>,
    private val lastBoxReset: Box<LastReset>,
) {
    suspend fun saveTask(task: Task): Long = withContext(Dispatchers.IO) {
        return@withContext taskBox.put(task)
    }

    suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) {
        val task = taskBox.get(id)
        return@withContext task
    }

    suspend fun taskGetAll(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext taskBox.all
    }


    suspend fun updateChildrenRepeatStatus(parentId: Long): Unit = withContext(
        Dispatchers.IO
    ) {
        val task = getTask(parentId) ?: return@withContext
        task.children.forEach { child ->
            val freshChild = getTask(child.id) ?: return@withContext
            val newTask =
                freshChild.copy(repeatType = task.repeatType, repeatDays = task.repeatDays)
            saveTask(newTask)
            updateChildrenRepeatStatus(freshChild.id)
        }
    }

    suspend fun updateDoneStatus(id: Long) = withContext(Dispatchers.IO) {
        val task = getTask(id) ?: return@withContext
        task.done = !task.done
        saveTask(task)
    }

    suspend fun skipTask(id: Long) = withContext(Dispatchers.IO) {
        val task = getTask(id) ?: return@withContext
        task.hide = true
        saveTask(task)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        return@withContext taskBox.remove(id)
    }

    suspend fun updateParentDoneStatus(id: Long): Unit = withContext(Dispatchers.IO) {
        val task = getTask(id) ?: return@withContext
        val parentId = task.parent.target?.id ?: return@withContext
        val freshParent = getTask(parentId) ?: return@withContext
        val allChildrenDone = freshParent.children.all { it.done }
        freshParent.done = allChildrenDone
        saveTask(freshParent)
        updateParentDoneStatus(freshParent.id)

    }

    suspend fun taskDeleteALl() = withContext(Dispatchers.IO) {
        return@withContext taskBox.removeAll()
    }

    suspend fun taskSaveList(tasks: List<Task>) = withContext(Dispatchers.IO) {
        return@withContext taskBox.put(tasks)
    }

    suspend fun getLastResetDate() = withContext(Dispatchers.IO) {
        var lastResetDate = lastBoxReset.get(1).lastResetDate
        if (lastResetDate == null) lastResetDate = getCurrentDate()
        return@withContext lastResetDate
    }

    suspend fun setLastResetToday(todayDate: String): Long = withContext(Dispatchers.IO) {
        val reset = LastReset(id = 1, lastResetDate = todayDate)
        lastBoxReset.put(reset)
    }

    suspend fun attach(task: Task) = withContext(Dispatchers.IO) {
        taskBox.attach(task)
    }
}