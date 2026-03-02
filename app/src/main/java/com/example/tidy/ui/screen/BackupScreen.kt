package com.example.tidy.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.tidy.Task
import io.objectbox.Box

@Composable
fun BackupScreen(
    taskBox: Box<Task>,
){
    Text(
        text = "Backup"
    )
}