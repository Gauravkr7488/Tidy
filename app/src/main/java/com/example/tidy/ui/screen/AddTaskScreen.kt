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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.WeekDays
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskIconAction
import com.example.tidy.viewModels.AddTaskScreenViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.tidy.sqldelight.Task

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
    var taskChildren by remember { mutableStateOf<List<Task>>(emptyList()) }
    var createdAt = ""
    val coroutineScope = rememberCoroutineScope()
    var repeatType by remember { mutableStateOf(RepeatTypes.NONE) }
    var repeatDays by remember { mutableStateOf("") }
    var showFab by remember { mutableStateOf(true) } // to make the transition to the home look better
    var showAlertDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val task = addTaskScreenViewModel.getCurrentTask(taskId = taskId)
        if (task != null) {
            taskId = task.id
            taskChildren = addTaskScreenViewModel.getChildren(task.id)
            taskTitle = task.title
            description = task.description
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (taskTitle != "") {
                                val savedTaskId = addTaskScreenViewModel.addTask(
                                    Task(
                                        id = taskId,
                                        title = taskTitle,
                                        repeatType = repeatType,
                                        repeatDays = repeatDays,
                                        description = description,
                                        done = 0,
                                        hide = 0,
                                        createdAt = System.currentTimeMillis(),
                                        parentId = null,
                                    )
                                )
                                taskChildren.forEach {
                                    addTaskScreenViewModel.addTask(
                                        it.copy(parentId = savedTaskId)
                                    )
                                }

                                showFab = false
                                navController.popBackStack()
                            } else {
                                @Suppress("AssignedValueIsNeverRead")
                                showAlertDialog = true
                            }
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
            RepeatMenu(
                repeatType = repeatType,
                repeatDays = repeatDays,
                onRepeatTypeChange = { repeatType = it },
                onRepeatDaysChange = { repeatDays = it },
            )
            SubTaskMenu(
                taskChildren,
                onRemoveSubTask = { task, deleteTask, deleteChildren ->
                    taskChildren = addTaskScreenViewModel.removeSubTask(
                        task,
                        taskChildren,
                        deleteTask,
                        deleteChildren
                    )
                },
                addChildrenWithTitle = {
                    val childTask = Task(
                        id = 0,
                        title = it,
                        repeatType = RepeatTypes.NONE,
                        repeatDays = "",
                        description = "",
                        done = 0,
                        hide = 0,
                        createdAt = System.currentTimeMillis(),
                        parentId = null,
                    )
                    taskChildren = taskChildren + childTask
                }
            )
            if (createdAt != "") {
                Text(
                    text = "Created $createdAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (showAlertDialog) {
                EmptyTitleDialog { showAlertDialog = false }
            }
        }
    }
}

@Composable
fun EmptyTitleDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Please Enter Title") },
        text = { Text("Task can not be saved without a title.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun RepeatMenu(
    repeatType: String,
    repeatDays: String,
    onRepeatTypeChange: (String) -> Unit,
    onRepeatDaysChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDateDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var expanded by remember { mutableStateOf(false) }

            Text("Repeat")
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Box {
                    TextButton(
                        onClick = { expanded = !expanded },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(repeatType, modifier = Modifier.widthIn(min = 60.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(
                            RepeatTypes.NONE,
                            RepeatTypes.DAILY,
                            RepeatTypes.WEEKLY,
                            RepeatTypes.MONTHLY
                        ).forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    onRepeatTypeChange(t)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
        if (repeatType == RepeatTypes.WEEKLY) {
            var selectedDays by remember {
                mutableStateOf(
                    repeatDays.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                )
            }

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
                            selectedDays =
                                if (day in selectedDays) selectedDays - day else selectedDays + day
                            onRepeatDaysChange(
                                selectedDays.joinToString(
                                    ", "
                                )
                            )

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
                Button(onClick = { showDateDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Select Date")
                }
            } else {
                val chips = repeatDays
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                val listState = rememberLazyListState()

                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chips) { chip ->
                        FilterChip(
                            onClick = {
                                val updated = chips.toMutableList().apply {
                                    remove(chip)
                                }
                                onRepeatDaysChange(updated.joinToString(","))
                            },
                            label = { Text(chip) },
                            selected = chip in repeatDays,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }

                Button(onClick = { showDateDialog = true }, modifier = Modifier.fillMaxWidth()) {
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
                            onRepeatDaysChange("$repeatDays,$formatted")
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskMenu(
    taskChildren: List<Task>,
    addChildrenWithTitle: (String) -> Unit,
    onRemoveSubTask: (Task, Boolean, Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    var subTaskTitle by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var subTaskForRemove by remember {
        mutableStateOf(
            Task(
                title = "",
                id = 0,
                repeatType = RepeatTypes.NONE,
                repeatDays = "",
                description = "",
                done = 0,
                hide = 0,
                createdAt = System.currentTimeMillis(),
                parentId = null,
            )
        )
    }
    var deleteTask by remember { mutableStateOf(false) }
    var deleteChildren by remember { mutableStateOf(false) }
    Column {
        Text("SubTasks")
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(bottom = 5.dp),
        ) {
            items(
                items = taskChildren,
            ) { item ->
                TaskCard(task = item, trailingIconButtons = buildList {
                    add(
                        TaskIconAction(
                            icon = Icons.Default.Close,
                            description = "Remove Task",
                            onClick = {
                                showDialog = true
                                subTaskForRemove = item
                            },
                        )
                    )
                })
            }
        }
        OutlinedTextField(
            value = subTaskTitle,
            onValueChange = { subTaskTitle = it },
            placeholder = { Text("Add Subtask") },
            keyboardOptions = KeyboardOptions(
                imeAction = if (subTaskTitle != "") ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    coroutineScope.launch {
                        addChildrenWithTitle(subTaskTitle)
                        subTaskTitle = ""
                        listState.animateScrollToItem(taskChildren.size)
                    }
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Remove SubTask") },
                text = {
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                append("Are you sure you want to remove '")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(subTaskForRemove.title)
                                }
                                append("'?")
                            }
                        )
                        if (subTaskForRemove.id != 0L) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { deleteTask = !deleteTask }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = deleteTask,
                                    onCheckedChange = { deleteTask = it }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Also delete subtask")
                            }
                            if (subTaskForRemove.children.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { deleteChildren = !deleteChildren }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = deleteChildren,
                                        onCheckedChange = { deleteChildren = it }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Also delete children")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onRemoveSubTask(subTaskForRemove, deleteTask, deleteChildren)
                        showDialog = false
                    }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
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
}