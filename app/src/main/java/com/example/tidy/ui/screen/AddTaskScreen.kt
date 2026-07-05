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

package com.example.tidy.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.tidy.Utils
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.Routes
import com.example.tidy.constants.WeekDays
import com.example.tidy.ui.component.buttons.OutlinedDropDownButton
import com.example.tidy.ui.component.buttons.RoundedOutlineButtonTidy
import com.example.tidy.ui.component.dialog.SimpleDialog
import com.example.tidy.ui.component.list.FadingLazyRow
import com.example.tidy.ui.component.menu.OutlinedMenuItem
import com.example.tidy.ui.component.pickers.DatePickerTidy
import com.example.tidy.ui.component.pickers.TimePickerTidy
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskIconAction
import com.example.tidy.ui.component.taskComponents.TaskSelectionDialog
import com.example.tidy.ui.component.topAppBar.TopAppBar
import com.example.tidy.viewModels.SharedViewModel
import com.tidy.sqldelight.Task
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun AddTaskScreen(
    sharedViewModel: SharedViewModel,
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
    var blockedByTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var createdAt = ""
    val coroutineScope = rememberCoroutineScope()
    var repeatType by remember { mutableStateOf(RepeatTypes.NONE) }
    var frequency by remember { mutableStateOf("1") }
    var repeatDays by remember { mutableStateOf("") }
    var showBottomButtons by remember { mutableStateOf(true) } // to make the transition to the home look better
    var showAlertDialog by remember { mutableStateOf(false) }
    var parentId: Long? by remember { mutableStateOf(null) }
    var hide: Long by remember { mutableLongStateOf(0) }
    var done: Long by remember { mutableLongStateOf(0) }
    var priority: Long? by remember { mutableStateOf(null) }
    var dueDate: Long? by remember { mutableStateOf(null) }
    var dueTime: Long? by remember { mutableStateOf(null) }
    var startDate: Long? by remember { mutableStateOf(null) }
    var endDate: Long? by remember { mutableStateOf(null) }
    var time: Long? by remember { mutableStateOf(null) }

    val createMoreStaus = sharedViewModel.createMoreStatus.collectAsState()
    LaunchedEffect(Unit) {
        val task = sharedViewModel.getTask(taskId = taskId)
        if (task != null) {
            taskId = task.id
            taskChildren = sharedViewModel.tasks.value.filter { it.parentId == task.id }
            blockedByTasks = sharedViewModel.getBlockedByTasks(taskId)
            parentId = task.parentId
            taskTitle = task.title
            description = task.description
            repeatType = task.repeatType
            hide = task.hide
            done = task.done
            priority = task.priority
            repeatDays = if (repeatType == RepeatTypes.NONE) "" else task.repeatDays
//            dueDateAndTime = task.dueDateAndTime
            createdAt =
                Utils.changeDateFormat(pattern = "MMM dd, yyyy hh:mm a", date = task.createdAt)
        }
        if (taskTitle.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    BackHandler(
        enabled = true
    ) {
        if (createMoreStaus.value) sharedViewModel.toggleCreateMoreStatus()
        navController.navigate(
            Routes.HOME,
            navOptions = navOptions {
                popUpTo(Routes.HOME) { inclusive = true }
                launchSingleTop = true
            }
        )
    }
    Scaffold(
        topBar =
            { TopAppBar(if (taskId == 0L) "Add Task" else "Edit Task") },
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (showBottomButtons) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (taskTitle != "") {
                                val savedTaskId = sharedViewModel.saveTask(
                                    Task(
                                        id = taskId,
                                        title = taskTitle,
                                        repeatType = repeatType,
                                        repeatDays = repeatDays,
                                        description = description,
                                        done = done,
                                        hide = hide,
                                        createdAt = System.currentTimeMillis(),
                                        parentId = parentId,
                                        blockStatus = if (blockedByTasks.all { it.done == 1L }) 0L else 1L,
                                        priority = priority,
                                        dueDateAndTime = dueDate
                                    )
                                )
                                if (savedTaskId == null) return@launch
                                blockedByTasks.forEach {
                                    val blockerId =
                                        if (it.id == 0L) sharedViewModel.saveTask(it) else it.id
                                    if (blockerId == null) {
                                        println("issue while saving new blocker") // todo better logging or errs
                                        return@forEach
                                    }
                                    sharedViewModel.addBlockedByTasks(savedTaskId, blockerId)
                                }
                                taskChildren.forEach {
                                    sharedViewModel.saveTask(
                                        it.copy(
                                            parentId = savedTaskId,
                                            repeatType = repeatType,
                                            repeatDays = repeatDays,
                                        )
                                    )
                                }

                                showBottomButtons = createMoreStaus.value
                                if (createMoreStaus.value) navController.navigate("${Routes.ADD_TASK}/${0}")
                                else navController.navigate(
                                    Routes.HOME,
                                    navOptions = navOptions {
                                        popUpTo(Routes.HOME) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                )
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
            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                singleLine = false,
                modifier = Modifier.fillMaxWidth()
            )
            RepeatMenu(
                repeatType = repeatType,
                repeatDays = repeatDays,
                onRepeatTypeChange = { repeatType = it },
                onRepeatDaysChange = { repeatDays = it },
            )
            ScheduleMenu(
                repeatType = repeatType,
                repeatDays = repeatDays.split(","),
                frequency = frequency,
                onRepeatTypeChange = { repeatType = it },
                onRepeatDaysChange = { repeatDays = it.joinToString(",") },
                onFrequencyNumberChange = { frequency = it },
                startDate = startDate,
                endDate = endDate,
                time = time,
                onStartDateChange = { startDate = it },
                onEndDateChange = { endDate = it },
                onTimeChange = { time = it },
                dueDate = dueDate,
                dueTime = dueTime,
                onDueDateChange = { dueDate = it },
                onDueTImeChange = { dueTime = it }
            )
            PriorityMenu(
                priorityValue = priority,
                onPriorityValueChange = { priority = it },
            )
//            DueMenu(dueDateAndTime, { dueDateAndTime = it })
            SubTaskMenu(
                taskChildren,
                onRemoveSubTask = { subTask, deleteTask, deleteChildren ->
                    taskChildren = sharedViewModel.removeSubTask(
                        subTask,
                        taskChildren,
                        deleteTask,
                        deleteChildren
                    )
                },
                addChildrenWithTitle = {
                    val childTask = Utils.getEmptyTask().copy(title = it)
                    taskChildren = taskChildren + childTask
                },
                getChild =
                    { id ->
                        sharedViewModel.tasks.value.filter { it.parentId == id }
                    }
            )
            BlockedByMenu(
                blockedByTasks = blockedByTasks,
                getChild = { id ->
                    sharedViewModel.tasks.value.filter { it.parentId == id }
                },
                availableTaskList = sharedViewModel.tasks.collectAsState().value.filter { it.id != taskId },
                onAdd = { blockedByTasks = blockedByTasks + it },
                onTaskRemove = { blockedByTasks = blockedByTasks - it },
            )
            if (createdAt != "") {
                Text(
                    text = "Created $createdAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (showBottomButtons && taskId == 0L) CreateMoreOption(checked = createMoreStaus.value) { sharedViewModel.toggleCreateMoreStatus() }
            if (showAlertDialog) {
                EmptyTitleDialog { showAlertDialog = false }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueMenu(
    dueDateAndTime: Long?,
    onDueDateAndTimeChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    var date by remember(dueDateAndTime) { mutableStateOf(dueDateAndTime) }
    var time by remember(dueDateAndTime) { mutableStateOf(dueDateAndTime) }
    var dateAndTime: Long? by remember(dueDateAndTime) { mutableStateOf(dueDateAndTime) }
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Due On")
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { expanded = !expanded },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (dateAndTime == null) "Pick" else Utils.changeDateFormat(
                        dateAndTime!!,
                        "hh:mm a 'On' MMM dd, yyyy"
                    ),
                    modifier = Modifier.widthIn(min = 60.dp)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            if (expanded) {
                SimpleDialog(
                    onDismissRequest = { expanded = false },
                    title = "Select Date and Time",
                    content = {
                        DropDownMenuTextButton(
                            { showDateDialog = true },
                            content = {
                                Text(
                                    if (date != null) {
                                        Utils.changeDateFormat(
                                            pattern = "MMM dd, yyyy",
                                            date = date!!
                                        )
                                    } else {
                                        "Today"
                                    }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.padding(5.dp)
                        )
                        DropDownMenuTextButton(
                            { showTimeDialog = true },
                            content = {
                                Text(
                                    if (time != null) {
                                        Utils.changeDateFormat(time!!, "hh:mm a")
                                    } else {
                                        "Select Time"
                                    }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            },
                            modifier = Modifier.padding(5.dp)

                        )
                    },
                    onConfirm = {
                        dateAndTime =
                            if (date == null && time == null) dueDateAndTime
                            else Utils.combineDateAndTimeMillis(date, time)
                        dateAndTime?.let {
                            if (it <= System.currentTimeMillis()) {
                                showAlertDialog = true
                                dateAndTime = null
                            } else {
                                onDueDateAndTimeChange(dateAndTime)
                                expanded = false
                            }
                        }
                    }
                )
            }
        }
    }
    if (showDateDialog) {
        DatePickerTidy(
            onDismiss = { showDateDialog = false },
            onDateSelected = {
                date = it
            },
            date = date
        )
    }
    if (showTimeDialog) {
        var hour: Int? = null
        var minute: Int? = null
        if (time != null) {
            hour = Utils.changeDateFormat(time!!, "HH")
                .toInt()
            minute = Utils.changeDateFormat(time!!, "mm")
                .toInt()
        }
        TimePickerTidy(
            hour = hour,
            minute = minute,
            onTimeSelected = {
                time = Utils.convertTimeToMillis(it.hour, it.minute)
            },
            onDismiss = { showTimeDialog = false }
        )
    }
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = { showAlertDialog = false },
            title = { Text("Past or Current time can not be selected") },
            text = { Text("Please select a time after the current time.") },
            confirmButton = {
                TextButton(onClick = { showAlertDialog = false }) {
                    Text("Ok")
                }
            }
        )
    }
}

@Composable
fun PriorityMenu(
    priorityValue: Long?,
    onPriorityValueChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var expanded by remember { mutableStateOf(false) }
            Text("Priority")
            Spacer(modifier = Modifier.weight(1f))
            Box {
                TextButton(
                    onClick = { expanded = !expanded },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        priorityValue?.toString() ?: "None",
                        modifier = Modifier.widthIn(min = 60.dp)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf(
                        "None" to null,
                        "1" to 1L,
                        "2" to 2L,
                        "3" to 3L,
                        "4" to 4L
                    ).forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onPriorityValueChange(value)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateMoreOption(checked: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Create More")
        Checkbox(checked = checked, onCheckedChange = {
            onClick()
        })
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
    var showAlert by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var expanded by remember { mutableStateOf(false) }

            Text("Repeat")
            Spacer(modifier = Modifier.weight(1f))
            Box {
                TextButton(
                    onClick = { expanded = !expanded },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        repeatType.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.widthIn(min = 60.dp)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf(
                        RepeatTypes.NONE,
                        RepeatTypes.DAY,
                        RepeatTypes.WEEK,
                        RepeatTypes.MONTH
                    ).forEach { t ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    t.lowercase().replaceFirstChar { it.uppercase() }
                                )
                            },
                            onClick = {
                                onRepeatTypeChange(t)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
        if (repeatType == RepeatTypes.WEEK) {
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
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                chips.forEach { (label, day) ->
                    FilterChip(
                        onClick = {
                            selectedDays =
                                if (day in selectedDays) selectedDays - day else selectedDays + day
                            onRepeatDaysChange(
                                selectedDays.joinToString(
                                    ","
                                )
                            )
                        },
                        label = { Text(label) },
                        selected = day in selectedDays,
                    )
                }
            }
        }
        if (repeatType == RepeatTypes.MONTH) {
            if (repeatDays == "") {
                Button(
                    onClick = { showDateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
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

                Button(
                    onClick = { showDateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                            if (formatted in repeatDays) {
                                showAlert = true
                            } else {
                                onRepeatDaysChange("$repeatDays,$formatted")
                            }

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
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Duplicate Date Chosen") },
            text = { Text("You have already selected this date, please chose different one.") },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("Ok")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleMenu(
    repeatType: String,
    repeatDays: List<String>,
    frequency: String,
    startDate: Long?,
    endDate: Long?,
    time: Long?,
    dueDate: Long?,
    dueTime: Long?,
    onRepeatTypeChange: (String) -> Unit,
    onRepeatDaysChange: (List<String>) -> Unit,
    onFrequencyNumberChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onTimeChange: (Long?) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onDueTImeChange: (Long?) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    OutlinedMenuItem("Schedule") {
        RoundedOutlineButtonTidy(
            "Add",
            onClick = { showDialog = true },
        )
    }
    if (showDialog) {
        SimpleDialog(
            onDismissRequest = { showDialog = false },
            title = "Add Schedule",
            onConfirm = { showDialog = false },
            showCancelButtons = false
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatSection(
                    repeatType = repeatType,
                    repeatDays = repeatDays,
                    onRepeatTypeChange = onRepeatTypeChange,
                    onRepeatDaysChange = onRepeatDaysChange,
                    frequency = frequency,
                    onFrequencyNumberChange = onFrequencyNumberChange,
                    startDate = startDate,
                    endDate = endDate,
                    time = time,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onTimeChange = onTimeChange
                )
                DueSection(
                    dueDate = dueDate,
                    dueTime = dueTime,
                    onDueDateChange = onDueDateChange,
                    onDueTImeChange = onDueTImeChange,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueSection(
    dueDate: Long?,
    dueTime: Long?,
    onDueDateChange: (Long?) -> Unit,
    onDueTImeChange: (Long?) -> Unit,
) {
    DateRow("Due Date", date = dueDate) {
        onDueDateChange(it)
    }
    if (dueDate != null) {
        TimeRow("DueTime", time = dueTime) {
            onDueTImeChange(it)
        }
    }
}

@Composable
private fun RepeatSection(
    repeatType: String,
    repeatDays: List<String>,
    frequency: String,
    startDate: Long?,
    endDate: Long?,
    time: Long?,
    onRepeatTypeChange: (String) -> Unit,
    onRepeatDaysChange: (List<String>) -> Unit,
    onFrequencyNumberChange: (String) -> Unit,
    onStartDateChange: (Long?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onTimeChange: (Long?) -> Unit
) {
    var showCustomMenu by remember { mutableStateOf(false) }
    val repeatTypeDisplayList = listOf(
        "None" to RepeatTypes.NONE,
        "Daily" to RepeatTypes.DAY,
        "Weekly" to RepeatTypes.WEEK,
        "Monthly" to RepeatTypes.MONTH,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Repeat Type",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(4.dp)
        )
        Box {
            var showDropDownMenu by remember { mutableStateOf(false) }
            OutlinedDropDownButton(
                label = if (showCustomMenu) "Custom" else repeatTypeDisplayList.first { it.second == repeatType }.first,
                onClick = { showDropDownMenu = true },
            )
            DropdownMenu(
                onDismissRequest = { showDropDownMenu = false },
                expanded = showDropDownMenu
            ) {
                repeatTypeDisplayList.forEach { (label, type) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onRepeatTypeChange(type)
                            showDropDownMenu = false
                            showCustomMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Custom") },
                    onClick = {
                        showDropDownMenu = false
                        showCustomMenu = true
                        onRepeatTypeChange(RepeatTypes.DAY)
                    }
                )
            }
        }
    }
    if (showCustomMenu) {
        CustomRow(
            frequencyNumber = frequency,
            frequencyType = repeatType,
            onFrequencyNumberChange = { onFrequencyNumberChange(it) },
            onFrequencyTypeChange = { onRepeatTypeChange(it) }
        )

    }

    if (repeatType == RepeatTypes.WEEK) {
        WeekDayRow(
            selectedDays = repeatDays
        ) { onRepeatDaysChange(it) }
    }
    if (repeatType == RepeatTypes.MONTH) {
        MonthRow(
            selectedDates = repeatDays
        ) { onRepeatDaysChange(it) }
    }

    if (showCustomMenu) {
        DateRow(
            "Start Date",
            date = startDate
        ) { onStartDateChange(it) }
        DateRow(
            "End Date",
            date = endDate
        ) { onEndDateChange(it) }
    }
    if (repeatType != RepeatTypes.NONE) {
        TimeRow(
            time = time,
            menuName = "Time"
        ) { onTimeChange(it) }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TimeRow(
    menuName: String,
    time: Long?,
    onTimeChange: (Long?) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    OutlinedMenuItem(menuName, onClick = { showTimePicker = true }) {
        if (time == null) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null
            )
        } else {
            Text(
                Utils.changeDateFormat(time, "hh:mm a")
            )
        }
    }
    if (showTimePicker) {
        var hour: Int? = null
        var minute: Int? = null
        if (time != null) {
            hour = Utils.changeDateFormat(time, "HH")
                .toInt()
            minute = Utils.changeDateFormat(time, "mm")
                .toInt()
        }
        TimePickerTidy(
            hour = hour,
            minute = minute,
            onTimeSelected = {
                onTimeChange(Utils.convertTimeToMillis(it.hour, it.minute))
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun DateRow(
    menuName: String,
    date: Long?,
    onDateChange: (Long?) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    OutlinedMenuItem(menuName, onClick = { showDatePicker = true }) {
        if (date == null) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null
            )
        } else {
            Text(
                Utils.changeDateFormat(
                    pattern = "MMM dd, yyyy",
                    date = date
                )
            )
        }
    }
    if (showDatePicker) {
        DatePickerTidy(
            date = date,
            onDateSelected = {
                onDateChange(it)
            }
        ) { showDatePicker = false }
    }
}

@Composable
private fun CustomRow(
    frequencyNumber: String,
    frequencyType: String,
    onFrequencyNumberChange: (String) -> Unit,
    onFrequencyTypeChange: (String) -> Unit,
) {
    var showFrequencyOptions by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        Text("Every")
        OutlinedTextField(
            value = frequencyNumber,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onFrequencyNumberChange(frequencyNumber)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
        )
        Box {
            val list = listOf(
                "Minute" to RepeatTypes.MINUTE,
                "Hour" to RepeatTypes.HOUR,
                "Day" to RepeatTypes.DAY,
                "Week" to RepeatTypes.WEEK,
                "Month" to RepeatTypes.MONTH,
                "Year" to RepeatTypes.YEAR
            )
            val frequencyTypeLabel = list.first {
                it.second == frequencyType
            }.first
            OutlinedDropDownButton(
                frequencyTypeLabel,
                onClick = { showFrequencyOptions = true },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)

            )
            DropdownMenu(expanded = showFrequencyOptions, onDismissRequest = {
                showFrequencyOptions = false
            }) {
                list.forEach { (label, frequency) ->
                    DropdownMenuItem(
                        text = {
                            Text(label)
                        },
                        onClick = {
                            onFrequencyTypeChange(frequency)
                            showFrequencyOptions = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthRow(
    selectedDates: (List<String>),
    onSelectedDateChange: (List<String>) -> Unit
) {
    var selectedDates by remember { mutableStateOf(selectedDates) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showDuplicateDateAlert by remember { mutableStateOf(false) }

    FadingLazyRow {
        selectedDates.forEach { date ->
            item {
                FilterChip(
                    onClick = {
                        selectedDates = selectedDates - date
                        onSelectedDateChange(selectedDates)
                    },
                    selected = date in selectedDates,
                    label = {
                        Text(date)
                    },
                    shape = RoundedCornerShape(50)
                )
            }
        }
        item {
            FilterChip(
                onClick = { showDateDialog = true },
                selected = false,
                label = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(50)
            )
        }
    }
    if (showDateDialog) {
        DatePickerTidy(
            onDismiss = { showDateDialog = false },
            onDateSelected = {
                if (it != null) {
                    val date = Utils.changeDateFormat(it, "dd")
                    if (date in selectedDates) {
                        showDuplicateDateAlert = true
                    } else {
                        selectedDates = selectedDates + date
                        onSelectedDateChange(selectedDates)
                    }
                }
            }
        )
    }
    if (showDuplicateDateAlert) {
        AlertDialog(
            onDismissRequest = { showDuplicateDateAlert = false },
            title = { Text("Duplicate Date Chosen") },
            text = { Text("You have already selected this date, please chose different one.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateDateAlert = false }) {
                    Text("Ok")
                }
            }
        )
    }
}

@Composable
private fun WeekDayRow(
    selectedDays: List<String>,
    onSelectedDayChange: (List<String>) -> Unit
) {
    var selectedDays by remember { mutableStateOf(selectedDays) }
    FadingLazyRow {
        listOf(
            "S" to WeekDays.SUN,
            "M" to WeekDays.MON,
            "T" to WeekDays.TUE,
            "W" to WeekDays.WED,
            "T" to WeekDays.THU,
            "F" to WeekDays.FRI,
            "S" to WeekDays.SAT,
        ).forEach { (label, day) ->
            item {
                FilterChip(
                    onClick = {
                        selectedDays =
                            if (day in selectedDays) selectedDays - day else selectedDays + day
                        onSelectedDayChange(selectedDays)
                    },
                    label = { Text(label) },
                    selected = day in selectedDays,
                    shape = RoundedCornerShape(50)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTaskMenu(
    taskChildren: List<Task>,
    addChildrenWithTitle: (String) -> Unit,
    onRemoveSubTask: (Task, Boolean, Boolean) -> Unit,
    getChild: (Long) -> List<Task>
) {
    val listState = rememberLazyListState()
    var subTaskTitle by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var subTaskForRemove by remember {
        mutableStateOf(
            Utils.getEmptyTask()
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
                TaskCard(
                    task = item,
                    trailingIconButtons = buildList {
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
                    },
                    children = getChild(item.id),
                )
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
                            val subTaskChildren = getChild(subTaskForRemove.id)
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
                            if (subTaskChildren.isNotEmpty()) {
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

@Composable
fun DropDownMenuTextButton( // skip transfer
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun BlockedByMenu(
    blockedByTasks: List<Task>,
    getChild: (Long) -> List<Task>,
    availableTaskList: List<Task>,
    onAdd: (List<Task>) -> Unit,
    onTaskRemove: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToRemove by remember { mutableStateOf(Utils.getEmptyTask()) }
    var showAddDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text("Blocked By")
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(bottom = 5.dp),
        ) {
            items(
                items = blockedByTasks
            ) { task ->
                TaskCard(
                    task = task,
                    trailingIconButtons = buildList {
                        add(
                            TaskIconAction(
                                icon = Icons.Default.Close,
                                description = "Remove Task",
                                onClick = {
                                    showDeleteDialog = true
                                    taskToRemove = task
                                },
                            )
                        )
                    },
                    children = getChild(task.id),
                )
            }
        }
        Button(onClick = { showAddDialog = true }) {
            Text("Add Blocked By")
        }
    }
    if (showAddDialog) {
        TaskSelectionDialog(
            tasks = availableTaskList,
            onConfirm = { selectedTasks ->
                var tasksToAdd: List<Task> = emptyList()
                selectedTasks.forEach {
                    if (!blockedByTasks.contains(it)) tasksToAdd = tasksToAdd + it
                }
                onAdd(tasksToAdd)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            title = { Text("Remove Task") },
            onDismissRequest = { showDeleteDialog = false },
            text = {
                Text(
                    text = buildAnnotatedString {
                        append("Are you sure you want to remove '")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(taskToRemove.title)
                        }
                        append("'?")
                    }
                )

            },
            confirmButton = {
                TextButton(onClick = {
                    onTaskRemove(taskToRemove)
                    showDeleteDialog = false
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}