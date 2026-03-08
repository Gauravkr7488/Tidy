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
    private var flag: String? = null
    private var taskId: Long? = null

    private var hostTaskIdRun: Long? = null

    private var updateFlag: Boolean = false

    fun getFlag(): String? {
        val f = flag
        flag = null
        return f
    }

    fun setId(id: Long) {
        taskId = id
    }

    fun getId(): Long? {
        val id = taskId
        taskId = null
        return id
    }

    fun setChildFlag() {
        flag = "child"
    }

    fun setParentFlag() {
        flag = "parent"
    }

    fun setUpdateFlag(){
        updateFlag = true
    }

    fun addNewChild(
        navController: NavController,
        ) {
        setChildFlag()
        navController.navigate(Routes.ADD_TASK)
    }

    fun saveTask(
        taskTitle: String,
        repeatDaily: Boolean,
        taskViewModel: TaskViewModel
    ): Long? {
        val flag = updateFlag
        updateFlag = false
        if (flag){
            val id = hostTaskIdRun ?: return null
            taskViewModel.updateTask(id, taskTitle, repeatDaily)
        }
        var id: Long? = null
        id = taskViewModel.tryTaskSave(taskTitle, repeatDaily)
        if (id == null) return null
        setId(id)
        return id
    }

    fun startAdoption(
        taskViewModel: TaskViewModel
    ){
        val id = getId()?: return
        val flag = getFlag() ?: return
        val hostTaskId = taskViewModel.tryTaskSave("placeHolder") ?: return
        if (flag == "child") {
            taskViewModel.adoption(id, hostTaskId)
            println("reaching here")
        }
        if (flag == "parent") taskViewModel.adoption( hostTaskId, id)
        setUpdateFlag()
        hostTaskIdRun = hostTaskId
    }
    fun addExistingChild(){
        /* later */
    }
}