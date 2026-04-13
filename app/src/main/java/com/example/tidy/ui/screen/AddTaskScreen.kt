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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
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
import java.util.Calendar

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
    var repeatType by remember { mutableStateOf(RepeatTypes.NONE) }
    var repeatDays by remember { mutableStateOf("") }
    var showDateDialog by remember { mutableStateOf(false) }
    var showFab by remember { mutableStateOf(true) }
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
                repeatDays = if (repeatType == RepeatTypes.NONE) "" else task.repeatDays
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
            if (showFab) {
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
                            @Suppress("AssignedValueIsNeverRead")
                            showFab = false
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .size(80.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save Task")
                }
            }
        }
    )
    { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "Add Task",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
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
                ) {
                    Text("Repeat")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = repeatStatus,
                        onCheckedChange = {
                            repeatStatus = it
                            repeatType = RepeatTypes.NONE
                        }
                    )
                }

                if (repeatStatus) {
                    val chips = listOf(
                        "Daily" to RepeatTypes.DAILY,
                        "Weekly" to RepeatTypes.WEEKLY,
                        "Monthly" to RepeatTypes.MONTHLY
                    )
                    Row {
                        chips.forEach { (label, type) ->
                            FilterChip(
                                onClick = { repeatType = type },
                                label = { Text(label) },
                                selected = repeatType == type,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                    }
                }
                if (repeatType == RepeatTypes.WEEKLY) {
                    var selectedDays by remember { mutableStateOf(setOf<String>()) }
                    repeatDays = selectedDays.joinToString(", ")

                    val chips = listOf(
                        "S" to WeekDays.SUN,
                        "M" to WeekDays.MON,
                        "T" to WeekDays.TUE,
                        "W" to WeekDays.WED,
                        "T" to WeekDays.THU,
                        "F" to WeekDays.FRI,
                        "S" to WeekDays.SAT,
                    )
                    Row {
                        chips.forEach { (label, day) ->
                            FilterChip(
                                onClick = {
                                    selectedDays = if (day in selectedDays)
                                        selectedDays - day  // deselect
                                    else
                                        selectedDays + day  // select
                                },
                                label = { Text(label) },
                                selected = day in selectedDays,
                                modifier = Modifier.padding(end = 10.dp)

                            )
                        }
                    }
                }
                if (repeatType == RepeatTypes.MONTHLY) {
                    if (repeatDays == "") {
                        Button(onClick = { showDateDialog = true }) {
                            Text("Select Date")
                        }
                    } else {

                        val chips = repeatDays
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        Row {
                            chips.forEach { chip ->
                                FilterChip(
                                    onClick = {
                                        val updated = chips.toMutableList().apply {
                                            remove(chip)
                                        }
                                        repeatDays = updated.joinToString(",")
                                    },
                                    label = { Text(chip) },
                                    selected = chip in repeatDays,
                                    modifier = Modifier.padding(end = 10.dp)
                                )
                            }
                        }

                        Button(onClick = { showDateDialog = true }) {
                            Text("Select more Dates")
                        }
                    }
                    val state = rememberDatePickerState()
                    if (showDateDialog) {
                        DatePickerDialog(
                            onDismissRequest = { showDateDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val milli = state.selectedDateMillis
                                    val calendar = Calendar.getInstance().apply {
                                        if (milli != null) {
                                            timeInMillis = milli
                                        }
                                    }
                                    val formatted =
                                        "%02d".format(calendar.get(Calendar.DAY_OF_MONTH))
                                    repeatDays = "$repeatDays,$formatted"
                                    showDateDialog = false
                                }) {
                                    Text("OK")
                                }
                            }
                        ) {
                            DatePicker(state = state)
                        }
                    }
                }
                SubTaskMenu(
                    "Subtasks",
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