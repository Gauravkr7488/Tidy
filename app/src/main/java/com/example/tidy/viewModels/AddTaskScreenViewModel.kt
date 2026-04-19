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
import com.example.tidy.DbOperation
import com.example.tidy.Task

class AddTaskScreenViewModel(
    private val dbOperation: DbOperation,
) : ViewModel() {
    private var childFlag: Boolean = false
    var parentTaskId: Long = 0
        private set

    suspend fun getCurrentTask(taskId: Long): Task? {
        if (parentTaskId != 0L && !childFlag) {
            val id = parentTaskId
            parentTaskId = 0
            return dbOperation.getTask(id)
        }
        if (taskId == 0L) return null
        return dbOperation.getTask(taskId)
    }

    suspend fun startAddNewChild(task: Task) {
        childFlag = true
        parentTaskId = dbOperation.saveTask(task)
    }

    suspend fun addTask(task: Task) {
        val i: Long = dbOperation.saveTask(task)
        dbOperation.updateChildrenRepeatStatus(i)
        if (parentTaskId != 0L) {
            dbOperation.addChild(childId = i, parentId = parentTaskId)
            childFlag = false
        }
    }
}