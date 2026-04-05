package com.example.tidy.viewModels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.tidy.Task
import com.example.tidy.constants.Routes

class SharedViewModel(
    private val navController: NavController,
) : ViewModel() {
    fun editTask(task: Task) {
        navController.navigate("${Routes.ADD_TASK}/${task.id}")
    }
}