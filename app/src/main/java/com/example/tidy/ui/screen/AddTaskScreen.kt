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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.tidy.Utils
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.Routes
import com.example.tidy.constants.WeekDays
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskIconAction
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
    var createdAt = ""
    val coroutineScope = rememberCoroutineScope()
    var repeatType by remember { mutableStateOf(RepeatTypes.NONE) }
    var repeatDays by remember { mutableStateOf("") }
    var showBottomButtons by remember { mutableStateOf(true) } // to make the transition to the home look better
    var showAlertDialog by remember { mutableStateOf(false) }
    var parentId: Long? by remember { mutableStateOf(null) }
    var hide: Long by remember { mutableLongStateOf(0) }
    var done: Long by remember { mutableLongStateOf(0) }
    var priority: Long? by remember { mutableStateOf(null) }
    var dueDateAndTime: Long? by remember { mutableStateOf(null) }

    val createMoreStaus = sharedViewModel.createMoreStatus.collectAsState()
    LaunchedEffect(Unit) {
        val task = sharedViewModel.getCurrentTask(taskId = taskId)
        if (task != null) {
            taskId = task.id
            taskChildren = sharedViewModel.tasks.value.filter { it.parentId == task.id }
            parentId = task.parentId
            taskTitle = task.title
            description = task.description
            repeatType = task.repeatType
            hide = task.hide
            done = task.done
            priority = task.priority
            repeatDays = if (repeatType == RepeatTypes.NONE) "" else task.repeatDays
            dueDateAndTime = task.dueDateAndTime
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
                                        priority = priority,
                                        dueDateAndTime = dueDateAndTime
                                    )
                                )
                                taskChildren.forEach {
                                    sharedViewModel.saveTask(
                                        it.copy(
                                            parentId = savedTaskId,
                                            repeatType = repeatType,
                                            repeatDays = repeatDays
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
            PriorityMenu(
                priorityValue = priority,
                onPriorityValueChange = { priority = it },
            )
            DueMenu(dueDateAndTime, { dueDateAndTime = it })
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
    var date by remember(dueDateAndTime) { mutableStateOf(dueDateAndTime) }
    var time by remember(dueDateAndTime) { mutableStateOf(dueDateAndTime) }
    var dateAndTime: Long? by remember { mutableStateOf(null) }
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
                    if (dateAndTime == null)"Pick" else Utils.changeDateFormat(dateAndTime!!, "hh:mm a 'On' MMM dd, yyyy"),
                    modifier = Modifier.widthIn(min = 60.dp)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            if (expanded) {
                SimpleDialog(
                    onDismissRequest = { expanded = false },
                    content = {
                        Text("Select Date and Time", modifier = Modifier.padding(5.dp))
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
                        dateAndTime = // todo clear
                            if (date == null && time == null) dueDateAndTime else {
                                val dateValue = date ?: Utils.getCurrentDateMillis()
                                val timeValue = time ?: 0L

                                val dateCalendar = Calendar.getInstance().apply {
                                    timeInMillis = dateValue
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }

                                val timeCalendar = Calendar.getInstance().apply {
                                    timeInMillis = timeValue
                                }

                                dateCalendar.apply {
                                    set(
                                        Calendar.HOUR_OF_DAY,
                                        timeCalendar.get(Calendar.HOUR_OF_DAY)
                                    )
                                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                                    set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND))
                                }.timeInMillis
                            }
                        onDueDateAndTimeChange(dateAndTime)
                    }
                )
            }
        }
    }
    if (showDateDialog) {
        DatePickerTidy(
            onDismiss = { showDateDialog = false },
            onDateSelected = {
                println(date)
                println(it)
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
                        RepeatTypes.DAILY,
                        RepeatTypes.WEEKLY,
                        RepeatTypes.MONTHLY
                    ).forEach { t ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    t.lowercase().replaceFirstChar { it.uppercase() })
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
                                    ", "
                                )
                            )
                        },
                        label = { Text(label) },
                        selected = day in selectedDays,
                    )
                }
            }
        }
        if (repeatType == RepeatTypes.MONTHLY) {
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
fun DropDownMenuTextButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SimpleTextButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
    ) {
        Text(text)
    }
}

@Composable
fun DatePickerTidy(
    date: Long?,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
        )
    }
}

@Composable
fun SimpleDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SimpleTextButton("Cancel") { onDismissRequest() }
                    SimpleTextButton("Ok") {
                        onConfirm()
                        onDismissRequest()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerTidy(
    hour: Int?,
    minute: Int?,
    onTimeSelected: (TimePickerState) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = hour ?: currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = minute ?: currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )
    SimpleDialog(
        onDismissRequest = onDismiss,
        onConfirm = { onTimeSelected(timePickerState) },
        content = {
            TimePicker(
                state = timePickerState
            )
        }
    )
}