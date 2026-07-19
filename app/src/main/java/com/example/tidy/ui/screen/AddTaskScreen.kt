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

import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.os.Build
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
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
    var frequencyNumber: String? by remember { mutableStateOf(null) }
    var repeatDays by remember { mutableStateOf("") }
    var showBottomButtons by remember { mutableStateOf(true) } // to make the transition to the home look better
    var showAlertDialog by remember { mutableStateOf(false) }
    var parentId: Long? by remember { mutableStateOf(null) }
    var hide: Long by remember { mutableLongStateOf(0) }
    var done: Long by remember { mutableLongStateOf(0) }
    var startNow by remember { mutableStateOf(false) }
    var repeatAfterDone by remember { mutableStateOf(false) }
    var priority: Long? by remember { mutableStateOf(null) }
    var dueDate: Long? by remember { mutableStateOf(null) }
    var dueTime: Long? by remember { mutableStateOf(null) }
    var endDate: Long? by remember { mutableStateOf(null) }
    var currentTask: Task? by remember { mutableStateOf(null) }
    val createMoreStaus = sharedViewModel.createMoreStatus.collectAsState()
    LaunchedEffect(Unit) {
        val task = sharedViewModel.getTask(taskId = taskId)
        if (task != null) {
            currentTask = task
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
            dueDate = task.dueDateAndTime
            dueTime = task.dueDateAndTime
            frequencyNumber = task.frequencyNumber
            endDate = task.endDate
            repeatAfterDone = task.repeatAfterDone == 1L
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
                                        dueDateAndTime = Utils.combineDateAndTimeMillis(
                                            dueDate,
                                            dueTime
                                        ),
                                        frequencyNumber = frequencyNumber,
                                        endDate = endDate,
                                        repeatAfterDone = if (repeatAfterDone) 1L else 0L,
                                    ), startNow
                                )
                                if (savedTaskId == null) return@launch
                                blockedByTasks.forEach {
                                    val blockerId =
                                        if (it.id == 0L) sharedViewModel.saveTask(it) else it.id
                                    if (blockerId == null) {
                                        println("issue while saving new blocker")
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

                                            ), startNow
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
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Title") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                singleLine = false,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            ScheduleMenu(
                repeatType = repeatType,
                repeatDays = if (repeatDays == "") emptyList() else repeatDays.split(","),
                frequencyNumber = frequencyNumber,
                onRepeatTypeChange = { repeatType = it },
                onRepeatDaysChange = { repeatDays = it.joinToString(",") },
                onFrequencyNumberChange = { frequencyNumber = it },
                endDate = endDate,
                onEndDateChange = { endDate = it },
                dueDate = dueDate,
                dueTime = dueTime,
                onDueDateChange = { dueDate = it },
                onDueTimeChange = { dueTime = it },
                startNow = startNow,
                repeatAfterDone = repeatAfterDone,
                onStartNowChange = { startNow = !startNow },
                onRepeatAfterDoneChange = { repeatAfterDone = !repeatAfterDone }
            )
            PriorityMenu(
                priorityValue = priority,
                onPriorityValueChange = { priority = it },
            )
            SubTaskMenu(
                taskChildren = taskChildren,
                getChild = { id ->
                    sharedViewModel.tasks.value.filter { it.parentId == id }
                },
                availableTaskList = if (currentTask == null) sharedViewModel.tasks.collectAsState().value else sharedViewModel.getAvailableSubTaskList(
                    currentTask!!
                ),
                onAdd = { taskChildren = taskChildren + it },
                onRemoveSubTask = { subTask, deleteTask, deleteChildren ->
                    taskChildren = sharedViewModel.removeSubTask(
                        subTask,
                        taskChildren,
                        deleteTask,
                        deleteChildren
                    )
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
            OutlinedMenuItem(
                "Priority",
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Box {
                    OutlinedDropDownButton(
                        onClick = { expanded = !expanded },
                        label = priorityValue?.toString() ?: "None",
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleMenu(
    repeatType: String,
    repeatDays: List<String>,
    frequencyNumber: String?,
    endDate: Long?,
    dueDate: Long?,
    dueTime: Long?,
    startNow: Boolean,
    repeatAfterDone: Boolean,
    onRepeatTypeChange: (String) -> Unit,
    onRepeatDaysChange: (List<String>) -> Unit,
    onFrequencyNumberChange: (String?) -> Unit,
    onEndDateChange: (Long?) -> Unit,
    onDueDateChange: (Long?) -> Unit,
    onDueTimeChange: (Long?) -> Unit,
    onStartNowChange: () -> Unit,
    onRepeatAfterDoneChange: () -> Unit,
) {
    val c = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    OutlinedMenuItem("Schedule", containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        RoundedOutlineButtonTidy(
            text = if (repeatType == RepeatTypes.NONE && dueDate == null) "Add" else "View",
            onClick = {
                Utils.requestExactAlarmPermission(c)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = c.getSystemService(ALARM_SERVICE) as AlarmManager
                    if (alarmManager.canScheduleExactAlarms()) {
                        showDialog = true
                    }
                }
            },
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

                var showCustomMenu by remember { mutableStateOf(frequencyNumber != null) }
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
                            label = if (frequencyNumber != null) "Custom" else repeatTypeDisplayList.first { it.second == repeatType }.first,
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
                                        onRepeatDaysChange(emptyList())
                                        onDueDateChange(null)
                                        onDueTimeChange(null)
                                        showDropDownMenu = false
                                        if (startNow) onStartNowChange()
                                        if (showCustomMenu) {
                                            showCustomMenu = false
                                            onFrequencyNumberChange(null)
                                            onEndDateChange(null)
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Custom") },
                                onClick = {
                                    showDropDownMenu = false
                                    showCustomMenu = true
                                    onRepeatTypeChange(RepeatTypes.DAY)
                                    onRepeatDaysChange(emptyList())
                                    if (startNow) onStartNowChange()
                                }
                            )
                        }
                    }
                }
                if (showCustomMenu || frequencyNumber != null) {
                    var f = frequencyNumber
                    if (f == null) {
                        f = "1"
                        onFrequencyNumberChange(f)
                    }
                    CustomRow(
                        frequencyNumber = f,
                        frequencyType = repeatType,
                        onFrequencyNumberChange = onFrequencyNumberChange,
                        onFrequencyTypeChange = onRepeatTypeChange
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
                        "Ends On",
                        date = endDate
                    ) { onEndDateChange(it) }
                    OutlinedMenuItem(
                        "Repeats after Completion",
                        onClick = { onRepeatAfterDoneChange() }
                    ) {
                        if (repeatAfterDone) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null
                            )
                        }
                    }

                }
                if (repeatType == RepeatTypes.NONE) {
                    DateRow(
                        menuName = "Due Date",
                        date = dueDate
                    ) { onDueDateChange(it) }
                }
                if (repeatType != RepeatTypes.NONE || dueDate != null) {
                    TimeRow(
                        time = dueTime,
                    ) { onDueTimeChange(it) }

                    OutlinedMenuItem("Starts Now", onClick = {
                        onStartNowChange()
                    }) {
                        if (startNow) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = null
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TimeRow(
    time: Long?,
    onTimeChange: (Long?) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    OutlinedMenuItem("On Time", onClick = { showTimePicker = true }) {
        if (time == null) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null
            )
        } else {
            Text(
                Utils.changeDateFormat(time, "hh:mm a")
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTimeChange(null) }
                    )
                }
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
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDateChange(null) }
                    )
                }
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
    ) {
        Text("Repeat Every")
        OutlinedTextField(
            value = frequencyNumber,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    onFrequencyNumberChange(newValue)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(60.dp)
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
                onClick = { showFrequencyOptions = true }
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

@Composable
fun SubTaskMenu(
    taskChildren: List<Task>,
    getChild: (Long) -> List<Task>,
    availableTaskList: List<Task>,
    onAdd: (List<Task>) -> Unit,
    onRemoveSubTask: (Task, Boolean, Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var subTaskForRemove by remember { mutableStateOf(Utils.getEmptyTask()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTask by remember { mutableStateOf(false) }
    var deleteChildren by remember { mutableStateOf(false) }
    OutlinedMenuItem(
        menuName = "Sub Tasks",
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        RoundedOutlineButtonTidy(
            text = if (taskChildren.isNotEmpty()) taskChildren.size.toString() else "Add",
            onClick = { showAddDialog = true }
        )
    }
    if (taskChildren.isNotEmpty()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .padding(bottom = 5.dp),
        ) {
            items(
                items = taskChildren
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
                                    subTaskForRemove = task
                                },
                            )
                        )
                    },
                    children = getChild(task.id),
                )
            }
        }
    }
    if (showAddDialog) {
        TaskSelectionDialog(
            tasks = availableTaskList,
            onConfirm = { selectedTasks ->
                var tasksToAdd: List<Task> = emptyList()
                selectedTasks.forEach {
                    if (!taskChildren.contains(it)) tasksToAdd = tasksToAdd + it
                }
                onAdd(tasksToAdd)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    showDeleteDialog = false
                }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
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

@Composable
fun BlockedByMenu(
    blockedByTasks: List<Task>,
    getChild: (Long) -> List<Task>,
    availableTaskList: List<Task>,
    onAdd: (List<Task>) -> Unit,
    onTaskRemove: (Task) -> Unit
) {
    val listState = rememberLazyListState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToRemove by remember { mutableStateOf(Utils.getEmptyTask()) }
    var showAddDialog by remember { mutableStateOf(false) }
    OutlinedMenuItem(
        menuName = "Blocked By",
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        RoundedOutlineButtonTidy(
            text = if (blockedByTasks.isNotEmpty()) blockedByTasks.size.toString() else "Add",
            onClick = { showAddDialog = true }
        )
    }
    if (blockedByTasks.isNotEmpty()) {
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