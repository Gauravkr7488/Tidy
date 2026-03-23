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

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.example.tidy.LastReset
import com.example.tidy.Task
import io.objectbox.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tidy.ExportManager
import com.example.tidy.TaskDto
import com.example.tidy.Task_
import com.example.tidy.toDto
import com.example.tidy.toTask
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskViewModel(
    private val taskBox: Box<Task>,
    private val lastBoxReset: Box<LastReset>,
    private val exportManager: ExportManager
) : ViewModel(){
    var tasks by mutableStateOf(taskBox.all.toList())
        private set

    fun refreshTasks() {
        resetTasksForToday()
        tasks = taskBox.query(Task_.hide.equal(false)).build().find()
    }

    fun cleanCompletedTasks() {
        taskBox.all
            .filter { it.done && it.parents.all { parent -> parent.done } }
            .forEach { task ->
                if (task.repeat) {
                    task.done = false
                    task.hide = true
                    taskBox.put(task)
                } else {
                    taskBox.remove(task.id) // delete one time tasks
                }
            }
        refreshTasks()
    }

    fun deleteTask(id: Long) {
        taskBox.remove(id)
        refreshTasks()
    }

    fun updateTaskDone(task: Task, isChecked: Boolean) {
        taskBox.put(task.copy(done = isChecked))
        task.parents.forEach { parent ->
            val allChildrenDone = parent.children.all { it.done }

            if (allChildrenDone) {
                taskBox.put(parent.copy(done = true))
            } else {
                taskBox.put(parent.copy(done = false))

            }
        }
        refreshTasks()
    }

    private fun resetTasksForToday() {
        val todayDate =
            SimpleDateFormat("dd", Locale.getDefault())
                .format(Calendar.getInstance().time)

        val existingReset = lastBoxReset.get(1)

        if (existingReset == null) {
            lastBoxReset.put(LastReset(id = 0, lastResetAt = todayDate))
            unhideAllTasks()
        } else if (existingReset.lastResetAt != todayDate) {
            existingReset.lastResetAt = todayDate
            lastBoxReset.put(existingReset)
            unhideAllTasks()
        }
    }

    private fun unhideAllTasks() {
        taskBox.all.forEach { task ->
            task.hide = false
            taskBox.put(task)
        }
    }

    fun tryTaskSave(
        taskTitle: String = "no name",
        note: Boolean = false,
        repeatDaily: Boolean = false,
        description: String = "",
    ): Long? {
        if (taskTitle.isBlank()) return null
        val newTask =
            Task(title = taskTitle, note = note, repeat = repeatDaily, description = description)
        return taskBox.put(newTask)
    }

    fun updateTask(
        taskId: Long,
        taskTitle: String,
        note: Boolean = false,
        repeatDaily: Boolean = false,
        description: String = "",
    ): Long? {
        if (taskTitle.isBlank()) return null

        val task = taskBox.get(taskId) ?: return null
        task.title = taskTitle
        task.note = note
        task.repeat = repeatDaily
        task.description = description
        println(task)

        return taskBox.put(task)

    }

    fun getTask(taskId: Long): Task? {
        val task = taskBox.get(taskId) ?: return null
        return task
    }

    fun createBackup(
        context: Context,
        uri: Uri
    ) {
        try {
            val tasks = taskBox.all
            val dtoTasks = tasks.map { it.toDto() }
            val json = Gson().toJson(dtoTasks)

            context.contentResolver
                .openOutputStream(uri)
                ?.use { stream ->
                    stream.write(json.toByteArray())
                }

            Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    fun importBackup(
        context: Context,
        uri: Uri
    ) {
        val oldTasks = taskBox.all

        try {
            val json = context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.readText()

            if (json != null) {
                val dtos = Gson().fromJson(
                    json,
                    Array<TaskDto>::class.java
                ).toList()


                val idMap = mutableMapOf<Long, Task>()

                val newTasks = dtos.map { dto ->
                    val task = dto.toTask()
                    idMap[dto.id] = task
                    task
                }

                taskBox.removeAll()
                taskBox.put(newTasks) // to get new ids

//              relation work
                dtos.forEach { dto ->
                    val task = idMap[dto.id] ?: return@forEach

                    val children = dto.childTasks?.mapNotNull { oldChildId ->
                        idMap[oldChildId]
                    } ?: emptyList()

                    task.children.addAll(children)
                }
                taskBox.put(newTasks) // to update relation

                Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            taskBox.removeAll()
            taskBox.put(oldTasks)
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun addChild(childId: Long, parentId: Long): Boolean {
        val child = taskBox.get(childId) ?: return false
        val parent = taskBox.get(parentId) ?: return false

        // Guard against self-referencing
        if (childId == parentId) return false

        parent.children.add(child)
        taskBox.put(parent)

        return true
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            exportManager.exportSilently()
        }
    }
}

class TaskViewModelFactory(
    private val taskBox: Box<Task>,
    private val lastBoxReset: Box<LastReset>,
    private val exportManager: ExportManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TaskViewModel(taskBox, lastBoxReset, exportManager) as T
    }
}