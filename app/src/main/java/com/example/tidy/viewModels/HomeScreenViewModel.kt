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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.DbOperation
import com.example.tidy.Task
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dbOperation: DbOperation,
) : ViewModel() {

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            tasks = dbOperation.taskGetAll()
        }
    }

    private suspend fun refreshTasks() {
        tasks = dbOperation.taskGetAll()
    }

    fun cleanCompletedTasks() {
        viewModelScope.launch {
            tasks
                .filter { it.done && it.parents.all { parent -> parent.done } }
                .forEach { task ->
                    if (task.repeat) {
                        task.done = false
                        task.hide = true
                        dbOperation.saveTask(task)
                    } else {
                        dbOperation.deleteTask(task.id) // delete one time tasks
                    }
                }
            refreshTasks()
        }
    }

    fun toggleDoneStatus(task: Task) {
        viewModelScope.launch {
            dbOperation.updateDoneStatus(task.id)
            dbOperation.updateParentDoneStatus(task.id)
            refreshTasks()
        }
    }

    fun skipTask(task: Task) {
        viewModelScope.launch {
            dbOperation.skipTask(task.id)
            refreshTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dbOperation.deleteTask(task.id)
            refreshTasks()
        }
    }
}