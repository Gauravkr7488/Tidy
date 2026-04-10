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

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.Task
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.Routes
import com.example.tidy.constants.WeekDays
import com.example.tidy.ui.component.SubTaskMenu
import com.example.tidy.viewModels.AddTaskScreenViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddTaskScreen(
    addTaskScreenViewModel: AddTaskScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
    taskId: Long = 0,
) {
    var taskId: Long = taskId
    var taskTitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var note by remember { mutableStateOf(false) }
    var repeatStatus by remember { mutableStateOf(false) }
    var taskChildren by remember { mutableStateOf<List<Task>>(emptyList()) }
    var createdAt = ""
    val coroutineScope = rememberCoroutineScope()
    var repeatType by remember { mutableStateOf("") }
    var repeatDays by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val task = addTaskScreenViewModel.getCurrentTask(taskId = taskId)
            if (task != null) {
                taskId = task.id
                taskChildren = task.children.toList()
                taskTitle = task.title
                description = task.description
                note = task.note
                repeatStatus = task.repeatType != RepeatTypes.NONE
                repeatType = task.repeatType
                repeatDays = if (repeatType == RepeatTypes.WEEKLY) task.repeatDays else ""
                val readable = SimpleDateFormat(
                    "MMM dd, yyyy hh:mm a",
                    Locale.getDefault()
                ).format(Date(task.createdAt))
                createdAt = readable
            }
            if (taskTitle.isEmpty()) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        addTaskScreenViewModel.addTask(
                            Task(
                                id = taskId,
                                title = taskTitle,
                                note = note,
                                repeatType = repeatType,
                                repeatDays = repeatDays,
                                description = description,
                            )
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .size(80.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save Task")
            }
        }
    )
    { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                },
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
                        checked = repeatStatus,
                        onCheckedChange = { repeatStatus = it }
                    )
                }

                if (repeatStatus) {
                    val chips = listOf(
                        "Daily" to RepeatTypes.DAILY,
                        "Weekly" to RepeatTypes.WEEKLY,
                        "Monthly" to RepeatTypes.MONTHLY
                    )

                    chips.forEach { (label, type) ->
                        FilterChip(
                            onClick = { repeatType = type },
                            label = { Text(label) },
                            selected = repeatType == type
                        )
                    }
                }
                if (repeatType == RepeatTypes.WEEKLY) {
                    var selectedDays by remember { mutableStateOf(setOf<String>()) }
                    repeatDays = selectedDays.joinToString(", ")

                    val chips = listOf(
                        "Mon" to WeekDays.MON,
                        "Tue" to WeekDays.TUE,
                        "Wed" to WeekDays.WED,
                        "Thu" to WeekDays.THU,
                        "Fri" to WeekDays.FRI,
                        "Sat" to WeekDays.SAT,
                        "Sun" to WeekDays.SUN
                    )
                    chips.forEach { (label, day) ->
                        FilterChip(
                            onClick = {
                                selectedDays = if (day in selectedDays)
                                    selectedDays - day  // deselect
                                else
                                    selectedDays + day  // select
                            },
                            label = { Text(label) },
                            selected = day in selectedDays
                        )
                    }
                }
                SubTaskMenu(
                    "Child Tasks",
                    {
                        coroutineScope.launch {
                            addTaskScreenViewModel.startAddNewChild(
                                Task(
                                    id = taskId,
                                    title = taskTitle,
                                    note = note,
                                    repeatType = repeatType,
                                    repeatDays = repeatDays,
                                    description = description,
                                )
                            )
                            navController.navigate(Routes.ADD_TASK)
                        }
                    },
                    taskChildren,
                    onTap = addTaskScreenViewModel::editTask,
                )
            }
            if (createdAt != "") {
                Text(
                    text = "Created $createdAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}