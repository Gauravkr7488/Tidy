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

package com.example.tidy.ui.component.subTaskComponents

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.example.tidy.Task
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskContextAction
import com.example.tidy.ui.component.taskComponents.TaskDeleteDialog
import com.example.tidy.ui.component.taskComponents.TaskIconAction


@Composable
fun SubTaskCard(
    task: Task,
    toggleDoneStatus: (Task) -> Unit,
    deleteTask: (Long, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onSkip: (Task) -> Unit,
    modifier: Modifier = Modifier,
    depth: Int = 0,
    last: Boolean = false,
    list: List<Boolean> = listOf(),
) {
    Column(
        modifier = modifier
    ) {
        var expanded by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 90f else 0f,
            label = "iconRotation"
        )
        var showDeleteDialog by remember { mutableStateOf(false) }
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            if (depth != 0) AddIndentation(last, list)
            TaskCard(
                task = task,
                onClick = {
                    if (task.children.isNotEmpty()) expanded =
                        !expanded else toggleDoneStatus(task)
                },
                contextMenuOptions = contextMenuOptions(
                    { onEdit(task) },
                    { onSkip(task) }
                ) { showDeleteDialog = true },
                leadingIcons =
                    buildList {
                        if (task.children.isNotEmpty()) {
                            add(
                                TaskIconAction(
                                    icon = Icons.Default.ChevronRight,
                                    description = "Expand Subtask",
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.rotate(rotation)
                                )
                            )
                        } else {
                            add(
                                TaskIconAction(
                                    icon = if (!task.done) Icons.Default.CheckBoxOutlineBlank else Icons.Default.CheckBox,
                                    description = "",
                                    onClick = { toggleDoneStatus(task) },
                                )
                            )
                        }
                    },
            )
        }
        if (showDeleteDialog) {
            TaskDeleteDialog(
                task = task,
                onDismiss = { showDeleteDialog = !showDeleteDialog },
                onDeleteClick = { deleteTask(task.id, it) }
            )
        }
        if (task.children.isNotEmpty() && expanded) {
            val bool =
                task == task.parent.target?.children?.lastOrNull() // is task last child
            val passingList =
                if (depth > 0) list + !bool else list // if task is last child then add false no line would be needed
            Column {
                task.children.forEach { child ->
                    key(child.id) {
                        SubTaskCard(
                            task = child,
                            depth = depth + 1,
                            last = child == task.children.last(),
                            list = passingList,
                            toggleDoneStatus = toggleDoneStatus,
                            deleteTask = deleteTask,
                            onEdit = onEdit,
                            onSkip = onSkip,
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun contextMenuOptions(
    onEdit: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit,
): List<TaskContextAction> {
    return listOf(
        TaskContextAction(
            label = "Edit",
            icon = Icons.Default.Create,
            description = "Edit Task",
            onClick = onEdit,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        TaskContextAction(
            label = "Skip",
            icon = Icons.Default.SkipNext,
            description = "Skip Task",
            onClick = onSkip,
            color = MaterialTheme.colorScheme.onTertiaryContainer

        ),
        TaskContextAction(
            label = "Delete",
            icon = Icons.Default.Delete,
            description = "Delete Task",
            onClick = onDelete,
            color = MaterialTheme.colorScheme.error
        )
    )
}