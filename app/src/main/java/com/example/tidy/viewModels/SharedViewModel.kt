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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SharedViewModel(
    private val dbOperation: DbOperation,
    private val exportManager: ExportManager,
) : ViewModel() {

    val tasks = dbOperation.observeTasks()
        .map { tasks -> sortByPriority(tasks) }
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

    fun sortByPriority(tasks: List<Task>): List<Task> {

        val sorted = tasks.sortedWith(compareBy {
            it.priority ?: Long.MAX_VALUE  // null sorts last
        })

        return sorted
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

    suspend fun getBlockedTasks(taskId: Long): List<Task> {
        return dbOperation.getBlockedTasks(taskId)
    }

    suspend fun getBlockedByTasks(taskId: Long): List<Task> {
        return dbOperation.getBlockedByTasks(taskId)
    }

    fun addBlockedByTasks(taskId: Long, blockerId: Long) {
        viewModelScope.launch {
            val x = dbOperation.getBlockedTask(taskId = taskId, blockerId = blockerId)
            if (x == null) dbOperation.addBlocker(taskId, blockerId)
        }
    }

    fun toggleDoneStatus(task: Task) {
        viewModelScope.launch {
            dbOperation.updateDoneStatus(task.id)
            if (task.parentId != null) dbOperation.updateParentDoneStatus(task.parentId)
            updateBlockedTasksStatus(
                task.id,
                task.done
            ) // task.done since the block is opposite of done
        }
    }

    suspend fun updateBlockedTasksStatus(taskId: Long, updatedBlockStatus: Long) {
        val blockedTasks = getBlockedTasks(taskId)
        blockedTasks.forEach {
            saveTask(it.copy(blockStatus = updatedBlockStatus))
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
        updateBlockedTasksStatus(task.id, 0)
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

    private suspend fun resetTasksForToday() { // todo this should be done via work manager so that even in bg it works
        val todayDate = getCurrentDate()
        val todayDay = getCurrentDay()

        val lastResetDate = dbOperation.getLastResetDate()
        if (lastResetDate == todayDate) return

        dbOperation.setLastResetToday(todayDate = todayDate)

        tasks.value.forEach { task ->
            val shouldReset = when (task.repeatType) {
                RepeatTypes.DAILY -> true
                RepeatTypes.WEEKLY -> task.repeatDays.contains(todayDay)
                RepeatTypes.MONTHLY -> task.repeatDays.contains(todayDate)
                else -> false
            }
            if (shouldReset) {
                dbOperation.saveTask(task.copy(hide = 0L, done = 0L))
            }

            if (task.repeatType == RepeatTypes.NONE) dbOperation.saveTask(task.copy(hide = 0L))
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            exportManager.exportSilently()
        }
    }

    suspend fun getTask(taskId: Long): Task? {
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
            if (task.parentId != null) dbOperation.saveTask(task.copy(parentId = null)) // to prevent saving of tasks that are removed before the saving of parent
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