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

import androidx.navigation.NavController
import com.example.tidy.constants.Routes

class AddTaskViewModel{
    private var taskId: Long? = null

    private var hostTaskId: Long? = null

    private var updateThisTask: Boolean = false

    private var addChild: Boolean = false

    fun setId(id: Long) {
        taskId = id
    }

    fun getId(): Long? {
        val id = taskId
        taskId = null
        return id
    }

    fun addNewChild(
        navController: NavController,
        ) {
        addChild = true
        navController.navigate(Routes.ADD_TASK)
    }

    fun saveTask(
        taskTitle: String,
        repeatDaily: Boolean,
        taskViewModel: TaskViewModel
    ): Long? {
        if (updateThisTask){
            updateThisTask = false
            val id = hostTaskId ?: return null
            hostTaskId = null
            return taskViewModel.updateTask(id, taskTitle, repeatDaily)
        }
        val id = taskViewModel.tryTaskSave(taskTitle, repeatDaily) ?: return null
        setId(id)
        return id
    }

    fun startAdoption(
        taskViewModel: TaskViewModel
    ){
        val id = getId()?: return
        val hostId = taskViewModel.tryTaskSave("placeHolder") ?: return
        if (addChild) {
            addChild = false
            taskViewModel.addChild(id, hostId)
        }
        updateThisTask = true
        hostTaskId = hostId
    }
    fun addExistingChild(){
        /* later */
    }
}