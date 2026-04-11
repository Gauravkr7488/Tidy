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
import androidx.navigation.NavController
import com.example.tidy.DbOperation
import com.example.tidy.Task
import com.example.tidy.constants.Routes

class AddTaskScreenViewModel(
    private val dbOperation: DbOperation,
    private val navController: NavController,
) : ViewModel() {
    private var parentTaskList: MutableList<Long> = mutableListOf()
    private var childFlag: Boolean = false


    suspend fun getCurrentTask(taskId: Long): Task? {
        var id: Long?
        if (parentTaskList.isNotEmpty() && !childFlag) {
            id = parentTaskList.last()
            parentTaskList.removeAt(parentTaskList.size - 1)
            return dbOperation.getTask(id)
        }
        id = taskId
        if (id == 0L) return null
        return dbOperation.getTask(id)
    }

    suspend fun startAddNewChild(task: Task) {
        val id = dbOperation.saveTask(task)
        childFlag = true
        parentTaskList.add(id)
    }

    suspend fun addTask(task: Task): Long {
        var i: Long
        i = dbOperation.saveTask(task)
        dbOperation.updateChildrenRepeatStatus(i)
        if (parentTaskList.isNotEmpty()) {
            i = task.id
            dbOperation.addChild(childId = i, parentId = parentTaskList.last())
            childFlag = false
        }
        return i
    }

    fun editTask(task: Task) {
        childFlag = true
        parentTaskList.add(task.parents.last().id)
        navController.navigate("${Routes.ADD_TASK}/${task.id}")
    }
}