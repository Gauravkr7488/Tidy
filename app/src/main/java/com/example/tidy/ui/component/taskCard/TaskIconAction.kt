package com.example.tidy.ui.component.taskCard

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.tidy.Task

data class TaskIconAction(
    val icon: ImageVector,
    val description: String,
    val onClick: (Task) -> Unit,
)
