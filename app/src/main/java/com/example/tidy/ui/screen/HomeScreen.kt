package com.example.tidy.ui.screen

import android.icu.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tidy.Task
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import io.objectbox.Box
import com.example.tidy.ui.component.TaskItem
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.setValue
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.tidy.LastReset
import com.example.tidy.MyObjectBox
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    taskBox: Box<Task>,
    lastResetBox: Box<LastReset>,
    modifier: Modifier = Modifier
) {
    var tasks by remember { mutableStateOf(taskBox.all) }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val listState = rememberLazyListState()
    var previousOffset by remember { mutableStateOf(0) }
    var previousIndex by remember { mutableStateOf(0) }

    var showFab by remember { mutableStateOf(true) }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to
                    listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->

            val scrollingUp =
                index < previousIndex ||
                        (index == previousIndex && offset < previousOffset)

            val scrollingDown =
                index > previousIndex ||
                        (index == previousIndex && offset > previousOffset)

            if (scrollingDown) {
                showFab = false
            } else if (scrollingUp) {
                delay(200)
                showFab = true
            }

            previousIndex = index
            previousOffset = offset
        }
    }

    val offsetY by animateDpAsState(
        targetValue = if (showFab) 0.dp else 300.dp
    )
    // This will run whenever HomeScreen is visible again
    LaunchedEffect(currentBackStackEntry) {
        tasks = getFreshTaskList(taskBox, lastResetBox)
    }
    val hasDoneTask = tasks.any { it.done }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.offset(y = offsetY)
            ) {

                // Delete FAB
                if (hasDoneTask) {
                    FloatingActionButton(
                        onClick = {
                            handleDelete(taskBox)
                            tasks = getFreshTaskList(taskBox, lastResetBox)
                        },
                        modifier = Modifier.size(80.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Completed"
                        )
                    }
                }

//                // Navigate FAB
                FloatingActionButton(
                    onClick = { navController.navigate("add_task") },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Add Task"
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp)
        ) {

            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp), // scrollable bottom space
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        taskBox = taskBox,
                        onRefresh = {
                            tasks = getFreshTaskList(taskBox, lastResetBox)
                        }
                    )
                }
            }
        }
    }
}


private fun handleDelete(taskBox: Box<Task>) {
    taskBox.all
        .filter { it.done }
        .forEach { task ->
            if (task.repeat) {
                task.done = false
                task.hide = true
                taskBox.put(task)
            } else {
                taskBox.remove(task.id)
            }
        }
}

private fun getFreshTaskList(taskBox: Box<Task>, lastResetBox: Box<LastReset>): List<Task> {
    resetTasksForToday(taskBox, lastResetBox)
    return taskBox.all.filter { !it.hide }
}

private fun resetTasksForToday(taskBox: Box<Task>, lastResetBox: Box<LastReset>) {
    val todayDate: String =
        SimpleDateFormat("dd", Locale.getDefault()).format(Calendar.getInstance().time)

    // Check if we have a reset record (we use ID 1 as our "singleton" record)
    val existingReset = lastResetBox.get(1)

    if (existingReset == null) {
        // First time ever: Create the record with ID 0 so ObjectBox assigns ID 1
        lastResetBox.put(LastReset(id = 0, lastResetAt = todayDate))
        unhideAllTasks(taskBox)
    } else if (existingReset.lastResetAt != todayDate) {
        // New day: Update the existing record and unhide tasks
        existingReset.lastResetAt = todayDate
        lastResetBox.put(existingReset)
        unhideAllTasks(taskBox)
    }
}

private fun unhideAllTasks(taskBox: Box<Task>) {
    taskBox.all.forEach { task ->
        if (task.hide) {
            task.hide = false
            taskBox.put(task)
        }
    }
}
