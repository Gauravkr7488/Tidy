package com.example.tidy.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tidy.DbOperation
import com.example.tidy.Task
import kotlinx.coroutines.launch

class NoteScreenViewModel(
    private val dbOperation: DbOperation,
) : ViewModel() {
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            tasks = dbOperation.taskGetAll()
        }
    }

    private suspend fun refreshTasks() {
        tasks = dbOperation.taskGetAll()
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dbOperation.deleteTask(task.id)
            refreshTasks()
        }
    }
}