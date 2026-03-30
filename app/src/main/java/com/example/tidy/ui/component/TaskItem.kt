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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.Task
import com.example.tidy.constants.Routes
import com.example.tidy.viewModels.AddTaskViewModel
import com.example.tidy.viewModels.TaskViewModel

@Composable
fun TaskItem(
    task: Task,
    viewModel: TaskViewModel,
    addTaskViewModel: AddTaskViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    isChild: Boolean = false,
) {
    if (task.parents.isNotEmpty() && !isChild) return
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            TaskRow(
                task = task,
                viewModel = viewModel,
                addTaskViewModel = addTaskViewModel,
                navController = navController,
                onDeleteConfirmed = { viewModel.deleteTask(task.id) },
                onSkip = { viewModel.skipTask(task.id) },
                onExpandClick = { expanded = !expanded },
                expanded = expanded,
            )
            if (task.children.isNotEmpty() && expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                task.children.forEach { child ->
                    TaskItem(
                        task = child,
                        viewModel = viewModel,
                        addTaskViewModel = addTaskViewModel,
                        navController = navController,
                        isChild = true,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
        }
    }
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun TaskRow(
    task: Task,
    viewModel: TaskViewModel,
    addTaskViewModel: AddTaskViewModel,
    navController: NavController,
    onDeleteConfirmed: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    onExpandClick: () -> Unit = {},
    expanded: Boolean = false,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    Box {
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
                                onLongPress = { offset ->
                                    tapOffset = offset
                                    showContextMenu = true
                                },
                                onTap = { }
                            )
                        }
                )
            }

            if (task.children.isNotEmpty()) {
                IconButton(onClick = { onExpandClick() }) {
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
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            offset = with(density) {
                DpOffset(tapOffset.x.toDp(), tapOffset.y.toDp())
            },
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            // Edit — placed first (most common, non-destructive)
            DropdownMenuItem(
                text = {
                    Text(
                        "Edit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    showContextMenu = false
                    addTaskViewModel.setCurrentTaskId(task.id)
                    navController.navigate(Routes.ADD_TASK)
                }
            )

            // Skip
            if (!task.note) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Skip",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.SkipNext,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        onSkip()
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Delete — last, separated by divider, colored red
            DropdownMenuItem(
                text = {
                    Text(
                        "Delete",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                },
                onClick = {
                    showContextMenu = false
                    showDeleteDialog = true
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteConfirmed()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}