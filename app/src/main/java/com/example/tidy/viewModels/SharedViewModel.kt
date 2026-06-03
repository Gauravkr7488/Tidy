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

import androidx.compose.foundation.lazy.LazyListState
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SharedViewModel(
    private val dbOperation: DbOperation,
    private val exportManager: ExportManager,
) : ViewModel() {

    val tasks = dbOperation.observeTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init {
        viewModelScope.launch {
            resetTasksForToday()
        }
    }

    fun cleanCompletedTasks() {
        viewModelScope.launch {
            val doneTasks = tasks.value.filter { it.done == 1L }
            doneTasks.forEach { task ->
                if (isRootTaskDoneOrArchived(task)) {
                    if (task.repeatType != RepeatTypes.NONE) {
                        val updatedTask = task.copy(
                            done = 0L,
                            hide = 1L
                        )
                        dbOperation.saveTask(updatedTask)
                    } else {
                        deleteTaskAndChildren(task.id)
                    }
                }
            }
        }
    }

    private suspend fun isRootTaskDoneOrArchived(task: Task): Boolean {
        if (task.parentId == null) return true
        val parent = dbOperation.getTask(task.parentId) ?: return true
        if (parent.hide == 1L) return true
        if (parent.done == 0L) isRootTaskDoneOrArchived(parent)
        return false
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
            dbOperation.saveTask(task.copy(hide = 1L))
            dbOperation.updateChildrenRepeatAndHideStatus(task.id)
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

        val repeatTasks = tasks.value.filter { it.repeatType != RepeatTypes.NONE }

        repeatTasks.forEach { task ->
            val shouldReset = when (task.repeatType) {
                RepeatTypes.NONE, RepeatTypes.DAILY -> true
                RepeatTypes.WEEKLY -> task.repeatDays.contains(todayDay)
                RepeatTypes.MONTHLY -> task.repeatDays.contains(todayDate)
                else -> false
            }
            if (shouldReset) {
                dbOperation.saveTask(task.copy(hide = 0L, done = 0L))
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

    suspend fun getCurrentTask(taskId: Long): Task? {
        if (taskId == 0L) return null
        return dbOperation.getTask(taskId)
    }

    suspend fun saveTask(task: Task): Long? {
        val i = dbOperation.saveTask(task) ?: return null
        dbOperation.updateChildrenRepeatAndHideStatus(i)
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

    private val _expandedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    val expandedTaskIds: StateFlow<Set<Long>> = _expandedTaskIds

    fun toggleExpanded(taskId: Long) {
        _expandedTaskIds.update { current ->
            if (taskId in current) current - taskId else current + taskId
        }
    }

    private val _createMoreStatus = MutableStateFlow(false)
    val createMoreStatus = _createMoreStatus.asStateFlow()

    fun toggleCreateMoreStatus() {
        _createMoreStatus.value = !_createMoreStatus.value
    }

    var listState: LazyListState? = null
}