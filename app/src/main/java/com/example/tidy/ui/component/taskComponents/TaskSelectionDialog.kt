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
package com.example.tidy.ui.component.taskComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tidy.Utils
import com.example.tidy.ui.component.dialog.SimpleDialog
import com.example.tidy.ui.component.textField.SearchTextField
import com.tidy.sqldelight.Task


@Composable
fun TaskSelectionDialog(
    tasks: List<Task>,
    onConfirm: (List<Task>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTasks: List<Task> by remember { mutableStateOf(emptyList()) }
    SimpleDialog(
        onDismissRequest = onDismiss,
        onConfirm = { onConfirm(selectedTasks) },
        title = if (selectedTasks.isEmpty()) "Select Tasks" else selectedTasks.size.toString() + " selected"
    ) {
        var query by remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        val filteredTasks = tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            return@filter matchesQuery
        }
        SearchTextField(
            query = query,
            placeHolder = "Search tasks",
            modifier = Modifier.padding(vertical = 8.dp)
        ) { query = it }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(bottom = 5.dp),
        ) {
            items(filteredTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = {
                        selectedTasks = if (selectedTasks.contains(task)) {
                            selectedTasks - task
                        } else {
                            selectedTasks + task
                        }
                    },
                    children = emptyList(),
                    trailingIcons = buildList {
                        if (selectedTasks.contains(task)) {
                            add(
                                TaskIconAction(
                                    icon = Icons.Default.Check,
                                    description = "selected",
                                    onClick = {},
                                )
                            )
                        }
                    }
                )
            }
            if (query != "") {
                item {
                    val t = Utils.getEmptyTask().copy(title = query)
                    TaskCard(
                        task = t,
                        onClick = {
                            selectedTasks = selectedTasks + t
                            query = ""
                        },
                        children = emptyList(),
                        trailingIcons =
                            buildList {
                                add(
                                    TaskIconAction(
                                        icon = Icons.Default.Create,
                                        description = "create new",
                                        onClick = {},
                                    )
                                )
                            },
                    )
                }
            }
        }
    }
}
