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

import io.objectbox.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DbOperation(
    private val taskBox: Box<Task>,
) {
    suspend fun saveTask(task: Task): Long = withContext(Dispatchers.IO) {
        return@withContext taskBox.put(task)
    }

    suspend fun getTask(id: Long): Task = withContext(Dispatchers.IO) {
        val task = taskBox.get(id)
        return@withContext task
    }

    suspend fun addChild(childId: Long, parentId: Long): Long? = withContext(Dispatchers.IO) {
        if (childId == parentId) return@withContext null
        val childTask = getTask(childId)
        val parentTask = getTask(parentId)
        parentTask.children.add(childTask)
        return@withContext saveTask(parentTask)
    }

    suspend fun updateDoneStatus(task: Task) = withContext(Dispatchers.IO){
        task.done = !task.done
        return@withContext saveTask(task)
    }

    suspend fun skipTask(task: Task) = withContext(Dispatchers.IO){
        task.hide = true
        return@withContext saveTask(task)
    }

    suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO){
        return@withContext taskBox.remove(task.id)
    }
}