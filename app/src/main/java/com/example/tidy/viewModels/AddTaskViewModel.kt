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

import com.example.tidy.DbOperation
import com.example.tidy.Task

class AddTaskViewModel(
    private val dbOperation: DbOperation
) {
    private var parentTaskList: MutableList<Long> = mutableListOf()
    private var currentTaskId: Long? = null
    private var childFlag: Boolean = false
    fun setCurrentTaskId(id: Long) {
        currentTaskId = id
    }

    fun getCurrentTaskId(): Long? {
        val id = currentTaskId
        currentTaskId = null
        return id
    }

    suspend fun getCurrentTask(): Task? {
        var id: Long?
        if (parentTaskList.isNotEmpty() && !childFlag) {
            id = parentTaskList.last()
            parentTaskList.removeAt(parentTaskList.size - 1)
            return dbOperation.getTask(id)
        }
        id = getCurrentTaskId()
        if (id == null) return null
        return dbOperation.getTask(id)
    }

    suspend fun startAddNewChild(task: Task) {
        val id = dbOperation.saveTask(task)
        childFlag = true
        if (parentTaskList.lastOrNull() != id) parentTaskList.add(id)
    }

    suspend fun addTask(task: Task): Long {
        var i: Long
        i = dbOperation.saveTask(task)
        if (parentTaskList.isNotEmpty()) {
            i = task.id
            dbOperation.addChild(childId = i, parentId = parentTaskList.last())
            childFlag = false
        }
        return i
    }
}