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
package com.example.tidy.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.Task
import com.example.tidy.ui.component.KeyboardAwareFAB
import com.example.tidy.ui.component.SubTaskMenu
import com.example.tidy.viewModels.AddTaskViewModel
import com.example.tidy.viewModels.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTaskScreen(
    taskViewModel: TaskViewModel,
    addTaskViewModel: AddTaskViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var note by remember { mutableStateOf(false) }
    var repeatDaily by remember { mutableStateOf(false) }
    var taskChildren by remember { mutableStateOf<List<Task>>(emptyList()) }
    var createdAt = ""
    LaunchedEffect(Unit) {
        addTaskViewModel.startAdoption(taskViewModel)
        taskChildren = addTaskViewModel
            .getHostChildren(taskViewModel)
            ?.toList()
            ?: emptyList()
        val details = addTaskViewModel.getTaskDetails(taskViewModel)
        if (details != null) {
            val (title, repeat, desc) = details
            taskTitle = title
            repeatDaily = repeat
            description = desc
        }
        if (taskTitle.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
        val t = addTaskViewModel.getTask(taskViewModel)
        if (t != null) {
            val readable = SimpleDateFormat(
                "MMM dd, yyyy hh:mm a",
                Locale.getDefault()
            ).format(Date(t.createdAt))
            createdAt = readable
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                KeyboardAwareFAB {
                    val id = addTaskViewModel.saveTask(
                        taskTitle,
                        repeatDaily,
                        description,
                        taskViewModel
                    )
                    if (id != null) navController.popBackStack()
                }
            }
        }
    )
    { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Spacer(modifier = Modifier.size(10.dp))
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Note")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = note,
                    onCheckedChange = { note = it }
                )
            }
            if (!note) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Repeat Daily")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = repeatDaily,
                        onCheckedChange = { repeatDaily = it }
                    )
                }
                SubTaskMenu(
                    "Child Tasks",
                    {
                        addTaskViewModel.addNewChild(
                            navController,
                            taskTitle,
                            repeatDaily,
                            description,
                            taskViewModel
                        )
                    },
                    { addTaskViewModel.addExistingChild() },
                    taskChildren
                )
            }
            if (createdAt != "") {
                Text(
                    text = "Created $createdAt",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}