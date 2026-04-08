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

@file:Suppress("AssignedValueIsNeverRead")

package com.example.tidy.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tidy.Task

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: (Task) -> Unit = {},
    onEditClick: (Task) -> Unit = {},
    onSkipClick: (Task) -> Unit = {},
    onDeleteClick: (Task) -> Unit = {},
    onExpandClick: (Long) -> Unit = {},
    expanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(expanded) }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically

        ) {
            TaskRow(
                title = task.title,
                doneStatus = task.done,
                showMenu = showMenu,
                onCloseMenu = { showMenu = false },
                onOpenMenu = { showMenu = true },
                modifier = Modifier
                    .weight(1f),
                onTap = {
                    if (task.children.isNotEmpty()) {
                        onExpandClick(task.id)
                        expanded = !expanded
                    }
                    else onClick(task)
                },
                menuContent = {
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
                            showMenu = false
                            onEditClick(task)
                        }
                    )

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
                                showMenu = false
                                onSkipClick(task)
                            }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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
                            showMenu = false
                            showDeleteDialog = true
                        }
                    )
                }
            )
            if (task.repeat) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repeat"
                )
            }
            if (task.children.isNotEmpty()) {
                IconButton(onClick = {
                    onExpandClick(task.id)
                    expanded = !expanded
                }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = null
                    )
                }
            }
        }

        if (task.children.isNotEmpty() && expanded) {
            Column {
                task.children.forEach { child ->
                    key(child.id) {
                        TaskCard(
                            task = child,
                            onClick = onClick,
                            onEditClick = onEditClick,
                            onSkipClick = onSkipClick,
                            onDeleteClick = onDeleteClick,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(task)
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