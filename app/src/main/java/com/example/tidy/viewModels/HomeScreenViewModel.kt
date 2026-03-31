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
import androidx.navigation.NavController
import com.example.tidy.DbOperation
import com.example.tidy.Task
import com.example.tidy.constants.Routes
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dbOperation: DbOperation,
    private val addTaskViewModel: AddTaskViewModel,
    private val navController: NavController,
    private  val taskViewModel: TaskViewModel, // TODO remove this
) : ViewModel() {
    fun toggleDoneStatus(task: Task) {
        viewModelScope.launch {
            dbOperation.updateDoneStatus(task)
            dbOperation.updateParentDoneStatus(task)
            taskViewModel.refreshTasks()
        }
    }

    fun editTask(task: Task) {
        addTaskViewModel.setCurrentTaskId(task.id)
        navController.navigate(Routes.ADD_TASK)
    }

    fun skipTask(task: Task){
        viewModelScope.launch {
            dbOperation.skipTask(task)
            taskViewModel.refreshTasks()
        }
    }

    fun deleteTask(task: Task){
        viewModelScope.launch {
            dbOperation.deleteTask(task)
            taskViewModel.refreshTasks()
        }
    }
}