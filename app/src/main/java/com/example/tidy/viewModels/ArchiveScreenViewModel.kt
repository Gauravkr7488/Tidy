package com.example.tidy.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.DbOperation
import com.example.tidy.Task
import kotlinx.coroutines.launch

class ArchiveScreenViewModel(
    private val dbOperation: DbOperation
) : ViewModel() {
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    suspend fun refreshTasks() {
        tasks = dbOperation.taskGetAll()
    }

    fun unarchiveTask(id: Long) {
        viewModelScope.launch {
            val task = dbOperation.getTask(id)
            val newTask = task.copy(hide = false)
            dbOperation.saveTask(newTask)
            refreshTasks()
        }
    }

    fun archiveTask(id: Long) {
        viewModelScope.launch {
            val task = dbOperation.getTask(id)
            val newTask = task.copy(hide = true)
            dbOperation.saveTask(newTask)
            refreshTasks()
        }
    }
}