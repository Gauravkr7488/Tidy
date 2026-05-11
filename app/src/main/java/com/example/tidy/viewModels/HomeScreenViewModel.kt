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
import androidx.navigation.NavController
import com.example.tidy.DbOperation
import com.example.tidy.ExportManager
import com.example.tidy.Task
import com.example.tidy.Utils.getCurrentDate
import com.example.tidy.Utils.getCurrentDay
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.Routes
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dbOperation: DbOperation,
    private val exportManager: ExportManager,
    private val navController: NavController,
) : ViewModel() {

    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            tasks.forEach { task -> repeatFix(task) } // old repeat migration, can be removed after one run
            refreshTasks()
            resetTasksForToday()
        }
    }

    suspend fun refreshTasks() {
        tasks = dbOperation.taskGetAll()
    }

    fun cleanCompletedTasks() {
        viewModelScope.launch {
            tasks.filter { it.done && allParentDone(it) }
                .forEach { task ->
                    if (task.repeatType != RepeatTypes.NONE) {
                        val updatedTask = task.copy(
                            done = false,
                            hide = true
                        )
                        dbOperation.saveTask(updatedTask)
                    } else {
                        dbOperation.deleteTask(task.id) // delete one time tasks
                    }
                }
            refreshTasks()
        }
    }

    private suspend fun allParentDone(task: Task): Boolean {
        val parentId = task.parent.target?.id ?: return true
        val parent = dbOperation.getTask(parentId)?: return false
        val parentDoneStatus = parent.done
        if (parentDoneStatus) allParentDone(parent)
        return false
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

    fun deleteTask(id: Long, deleteSubtasks: Boolean) {
        viewModelScope.launch {
            deleteTaskAsync(id, deleteSubtasks)
            refreshTasks()
        }
    }

    private suspend fun deleteTaskAsync(id: Long, deleteSubtasks: Boolean) {
        val task = dbOperation.getTask(id)?: return
        if (deleteSubtasks) {
            task.children.forEach { task ->
                deleteTaskAsync(task.id, true)
            }
        }
        val parentId = task.parent.target?.id
        dbOperation.deleteTask(task.id)
        updateParentStatus(parentId)
    }

    private suspend fun updateParentStatus(parentId: Long?) {
        if (parentId != null) { // update parent status
            val parent = dbOperation.getTask(parentId)?: return
            parent.done = parent.children.all { it.done }
            dbOperation.saveTask(parent)
            dbOperation.updateParentDoneStatus(parentId)
        }
    }

    private suspend fun resetTasksForToday() {
        val todayDate = getCurrentDate()
        val todayDay = getCurrentDay()

        val lastResetDate = dbOperation.getLastResetDate()
        if (lastResetDate == todayDate) return

        dbOperation.setLastResetToday(todayDate = todayDate)

        val hiddenTasks = dbOperation.taskGetAll().filter { task -> task.hide }
        hiddenTasks.forEach { task ->
            val shouldUnhide = when (task.repeatType) {
                RepeatTypes.NONE, RepeatTypes.DAILY -> true
                RepeatTypes.WEEKLY -> task.repeatDays.contains(todayDay)
                RepeatTypes.MONTHLY -> task.repeatDays.contains(todayDate)
                else -> false
            }
            if (shouldUnhide) {
                dbOperation.saveTask(task.copy(hide = false))
            }
        }
        refreshTasks()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            exportManager.exportSilently()
        }
    }

    fun editTask(task: Task) {
        navController.navigate("${Routes.ADD_TASK}/${task.id}")
    }

    suspend fun repeatFix(task: Task) {
        var newTask: Task = task
        when (task.repeatType) {
            "daily" -> newTask = task.copy(repeatType = RepeatTypes.DAILY)
            "weekly" -> newTask = task.copy(repeatType = RepeatTypes.WEEKLY)
            "monthly" -> newTask = task.copy(repeatType = RepeatTypes.MONTHLY)
        }
        dbOperation.saveTask(newTask)
    }
}