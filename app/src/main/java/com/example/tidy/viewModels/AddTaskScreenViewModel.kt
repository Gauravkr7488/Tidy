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

package com.example.tidy.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.DbOperation
import kotlinx.coroutines.launch
import com.tidy.sqldelight.Task

class AddTaskScreenViewModel(
    private val dbOperation: DbOperation,
) : ViewModel() {
    suspend fun getCurrentTask(taskId: Long): Task? {
        if (taskId == 0L) return null
        return dbOperation.getTask(taskId)
    }

    suspend fun addTask(task: Task): Long? {
        val i = dbOperation.saveTask(task) ?: return null
        dbOperation.updateChildrenRepeatStatus(i)
        return i
    }

    fun removeSubTask(
        task: Task,
        childrenList: List<Task>,
        deleteTask: Boolean,
        deleteChildren: Boolean
    ): MutableList<Task> {
        val list = childrenList.toMutableList()

        viewModelScope.launch {
            list.remove(task)
            dbOperation.saveTask(task.copy(parentId = null))
            if (deleteTask) {
                if (deleteChildren) {
                    deleteTaskAndChildren(task.id)
                } else {
                    dbOperation.deleteTask(task.id)
                }
            }
        }
        return list
    }

    private suspend fun deleteTaskAndChildren(id: Long) {
        dbOperation.getTask(id) ?: return
        val children = dbOperation.getChildren(id)
        if (children.isNotEmpty()) children.forEach { deleteTaskAndChildren(it.id) }
        dbOperation.deleteTask(id)
    }

    fun getChildren(id: Long): List<Task> {
        var list = emptyList<Task>()
        viewModelScope.launch {
            list = dbOperation.getChildren(id)
        }
        return list
    }
}