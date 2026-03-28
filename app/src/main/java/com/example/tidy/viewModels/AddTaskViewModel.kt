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

import com.example.tidy.Task
import com.example.tidy.TaskInfo
import io.objectbox.relation.ToMany

class AddTaskViewModel {
    private var taskId: Long? = null

    private var hostTaskId: Long? = null

    private var updateThisTask: Boolean = false

    private var addChild: Boolean = false

    fun setId(id: Long) {
        taskId = id
    }

    fun resetFlags() {
        taskId = null
        hostTaskId = null
        updateThisTask = false
        addChild = false
    }

    fun getHostChildren(taskViewModel: TaskViewModel): ToMany<Task>? {
        if (addChild) return null // TODO child or child
        val id = this.hostTaskId ?: return null
        val task = taskViewModel.getTask(id) ?: return null
        return task.children
    }

    fun getTaskDetails(taskViewModel: TaskViewModel): TaskInfo? {
        if (addChild) return null // TODO child or child
        val id = this.hostTaskId ?: return null
        val task = taskViewModel.getTask(id) ?: return null
        return TaskInfo(
            title = task.title,
            description = task.description,
            note = task.note,
            repeat = task.repeat
        )
    }

    fun getId(): Long? {
        val id = taskId
        taskId = null
        return id
    }

    fun addNewChild(
        taskTitle: String,
        note: Boolean,
        repeatDaily: Boolean,
        description: String,
        taskViewModel: TaskViewModel
    ) {
        if (this.hostTaskId == null) {
            this.hostTaskId =
                taskViewModel.tryTaskSave(taskTitle, note, repeatDaily, description = description)
                    ?: return // TODO add err
        } else {
            val i = this.hostTaskId ?: return
            taskViewModel.updateTask(i, taskTitle, note, repeatDaily, description = description)
                ?: return
        }
        addChild = true
    }
    fun saveTask(
        taskTitle: String,
        note: Boolean,
        repeatDaily: Boolean,
        description: String,
        taskViewModel: TaskViewModel
    ): Long? {
        if (updateThisTask && !addChild) { // TODO when child of child
            updateThisTask = false
            val id = hostTaskId ?: return null
            hostTaskId = null
            return taskViewModel.updateTask(id, taskTitle, note, repeatDaily, description)
        }
        val id = taskViewModel.tryTaskSave(taskTitle, note, repeatDaily, description) ?: return null
        setId(id)
        return id
    }

    fun startAdoption(
        taskViewModel: TaskViewModel
    ): Boolean {
        val id = getId() ?: return false
        val hostId = this.hostTaskId ?: return false
        if (addChild) {
            addChild = false
            taskViewModel.addChild(id, hostId)
        }
        updateThisTask = true
        return true
    }

    fun setUpdateState(id: Long) {
        this.updateThisTask = true
        hostTaskId = id
    }

    fun getTask(taskViewModel: TaskViewModel): Task? {
        val id = this.hostTaskId ?: return null
        val task = taskViewModel.getTask(id) ?: return null
        return task
    }
}