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
package com.example.tidy.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.Task
import com.example.tidy.constants.Routes
import com.example.tidy.viewModels.AddTaskViewModel
import com.example.tidy.viewModels.TaskViewModel

@Composable
@Suppress("AssignedValueIsNeverRead")
fun TaskItem(
    task: Task,
    viewModel: TaskViewModel,
    addTaskViewModel: AddTaskViewModel,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    if (task.parents.isNotEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Parent task row
            TaskRow(
                task = task,
                viewModel = viewModel,
                addTaskViewModel = addTaskViewModel,
                navController = navController,
                onLongPress = { showDialog = true },
                onExpandClick = { expanded = !expanded },
                expanded = expanded
            )

            // Children
            if (task.children.isNotEmpty() && expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                task.children.forEach { child ->
                    TaskRow(
                        task = child,
                        viewModel = viewModel,
                        addTaskViewModel = addTaskViewModel,
                        navController = navController,
                        onLongPress = { showDialog = true },
                        modifier = Modifier.padding(start = 24.dp) // indent children
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(

            onDismissRequest = { showDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task.id)
                    showDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskRow(
    task: Task,
    viewModel: TaskViewModel,
    addTaskViewModel: AddTaskViewModel,
    navController: NavController,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {},
    expanded: Boolean = false,
) {

    Row(
        modifier = modifier
            .padding(8.dp)
            .heightIn(min = 35.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onLongPress() },
                            onTap = {
                                addTaskViewModel.setUpdateState(task.id)
                                navController.navigate(Routes.ADD_TASK)
                            }
                        )
                    }
            )
        }
        if (task.children.isNotEmpty()) {
            IconButton(
                onClick = { onExpandClick() }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = null
                )
            }
        } else {
            if (!task.note) {
                Checkbox(
                    checked = task.done,
                    onCheckedChange = { isChecked ->
                        viewModel.updateTaskDone(task, isChecked)
                    }
                )
            }
        }

    }
}