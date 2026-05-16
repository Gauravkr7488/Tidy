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
import com.example.tidy.ExportManager
import com.example.tidy.Utils.getCurrentDate
import com.example.tidy.Utils.getCurrentDay
import com.example.tidy.constants.RepeatTypes
import com.tidy.sqldelight.Task
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val dbOperation: DbOperation,
    private val exportManager: ExportManager,
) : ViewModel() {

    val tasks = dbOperation.observeTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    val visibleTasks = tasks
        .map { it.filter { task -> task.parentId == null && task.hide == 0L } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            tasks.value.forEach { task -> repeatFix(task) }
            resetTasksForToday()
        }
    }

    fun cleanCompletedTasks() {
        viewModelScope.launch {
            tasks.value.filter { it.done == 1L && it.parentId == null }
                .forEach { task ->
                    if (task.repeatType != RepeatTypes.NONE) {
                        val updatedTask = task.copy(
                            done = 0L,
                            hide = 1L
                        )
                        dbOperation.saveTask(updatedTask)
                    } else {
                        deleteTaskAndChildren(task.id) // delete one time tasks
                    }
                }

        }
    }

    private suspend fun deleteTaskAndChildren(id: Long) {
        dbOperation.getTask(id) ?: return
        val children = tasks.value.filter { it.parentId == id }
        if (children.isNotEmpty()) children.forEach { deleteTaskAndChildren(it.id) }
        dbOperation.deleteTask(id)
    }

    fun toggleDoneStatus(task: Task) {
        viewModelScope.launch {
            dbOperation.updateDoneStatus(task.id)
            val parentId = task.parentId ?: return@launch
            dbOperation.updateParentDoneStatus(parentId)

        }
    }

    fun skipTask(task: Task) {
        viewModelScope.launch {
            dbOperation.skipTask(task.id)

        }
    }

    fun deleteTask(id: Long, deleteSubtasks: Boolean) {
        viewModelScope.launch {
            deleteTaskAsync(id, deleteSubtasks)

        }
    }

    private suspend fun deleteTaskAsync(id: Long, deleteSubtasks: Boolean) {
        val task = dbOperation.getTask(id) ?: return
        val children = tasks.value.filter { it.parentId == id }
        if (deleteSubtasks) {
            children.forEach { task ->
                deleteTaskAsync(task.id, true)
            }
        }
        val parentId = task.parentId
        dbOperation.deleteTask(task.id)
        updateParentStatus(parentId)
    }

    private suspend fun updateParentStatus(parentId: Long?) {
        if (parentId != null) { // update parent status
            val parent = dbOperation.getTask(parentId) ?: return
            val parentChildren = tasks.value.filter { it.parentId == parentId }
            val parentStatus = parentChildren.all { it.done == 0L }
            dbOperation.saveTask(parent.copy(done = if (!parentStatus) 0L else 1L))
            dbOperation.updateParentDoneStatus(parentId)
        }
    }

    private suspend fun resetTasksForToday() {
        val todayDate = getCurrentDate()
        val todayDay = getCurrentDay()

        val lastResetDate = dbOperation.getLastResetDate()
        if (lastResetDate == todayDate) return

        dbOperation.setLastResetToday(todayDate = todayDate)

        val hiddenTasks = dbOperation.taskGetAll().filter { task -> task.hide == 1L }
        hiddenTasks.forEach { task ->
            val shouldUnhide = when (task.repeatType) {
                RepeatTypes.NONE, RepeatTypes.DAILY -> true
                RepeatTypes.WEEKLY -> task.repeatDays.contains(todayDay)
                RepeatTypes.MONTHLY -> task.repeatDays.contains(todayDate)
                else -> false
            }
            if (shouldUnhide) {
                dbOperation.saveTask(task.copy(hide = 0L))
            }
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            exportManager.exportSilently()
        }
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