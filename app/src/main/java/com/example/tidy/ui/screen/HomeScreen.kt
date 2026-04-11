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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tidy.Task
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.taskComponents.TaskCardNew
import com.example.tidy.ui.component.taskComponents.TaskContextAction
import com.example.tidy.ui.component.taskComponents.TaskDeleteDialog
import com.example.tidy.ui.component.taskComponents.TaskIconAction
import com.example.tidy.viewModels.HomeScreenViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel,
    navController: NavController,

    modifier: Modifier = Modifier
) {
    val tasks =
        homeScreenViewModel.tasks.filter { task -> !task.note && task.parents.isEmpty() && !task.hide }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isOnTop = currentBackStackEntry?.destination?.route == Routes.HOME
    val listState = rememberLazyListState()
    var previousOffset by remember { mutableIntStateOf(0) }
    var previousIndex by remember { mutableIntStateOf(0) }

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
            @Suppress("AssignedValueIsNeverRead")
            previousIndex = index
            @Suppress("AssignedValueIsNeverRead")
            previousOffset = offset
        }
    }

    val offsetY by animateDpAsState(
        targetValue = if (showFab) 0.dp else 300.dp
    )
    LaunchedEffect(isOnTop) {
        if (isOnTop) {
            homeScreenViewModel.refreshTasks()
            @Suppress("AssignedValueIsNeverRead")
            showFab = true
        }
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

                if (hasDoneTask) {
                    FloatingActionButton(
                        onClick =
                            { homeScreenViewModel.cleanCompletedTasks() },
                        modifier = Modifier.size(80.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Completed"
                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        navController.navigate("${Routes.ADD_TASK}/${0}")
                    },
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
        ) {
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (tasks.isEmpty()) {
                EmptyTaskList()
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 150.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tasks, key = { it.id }) { task ->
                        if (task.children.isEmpty()) {
                            var showDeleteDialog by remember { mutableStateOf(false) }
                            TaskCardNew(
                                task = task,
                                onClick = {
                                    homeScreenViewModel.toggleDoneStatus(task)
                                },
                                contextMenuOptions = contextMenuOptions(
                                    task,
                                    homeScreenViewModel
                                ) { showDeleteDialog = true },
                            )
                            if (showDeleteDialog) {
                                TaskDeleteDialog(
                                    task = task,
                                    onDismiss = { showDeleteDialog = !showDeleteDialog },
                                    onDeleteClick = homeScreenViewModel::deleteTask
                                )
                            }
                        } else {
                            SubTaskCard(
                                task, homeScreenViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTaskList(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.List,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Add Some Tasks",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun contextMenuOptions(
    task: Task,
    homeScreenViewModel: HomeScreenViewModel,
    onDeleteClick: () -> Unit,
): List<TaskContextAction> {
    return listOf(
        TaskContextAction(
            label = "Edit",
            icon = Icons.Default.Create,
            description = "Edit Task",
            onClick = { homeScreenViewModel.editTask(task) }
        ),
        TaskContextAction(
            label = "Skip",
            icon = Icons.Default.SkipNext,
            description = "Skip Task",
            onClick = { homeScreenViewModel.skipTask(task) }
        ),
        TaskContextAction(
            label = "Delete",
            icon = Icons.Default.Delete,
            description = "Delete Task",
            onClick = onDeleteClick,
            color = MaterialTheme.colorScheme.error
        )
    )
}

@Composable
fun IndentationLines(depth: Int, last: Boolean, parentLast: Boolean) {
    Row {
        // spacers for ancestor levels
        repeat(depth - 1) {
            if (!parentLast) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(16.dp)
                    ) {
                        val end = if (last) size.height / 2 else size.height
                        drawLine(
                            color = Color.Gray,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2f
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight()
                )
            }
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
            )
        }
        if (depth > 0) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(16.dp)
                ) {
                    val end = if (last) size.height / 2 else size.height
                    drawLine(
                        color = Color.Gray,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, end),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}

@Composable
fun Alpha(last: Boolean, map: List<Boolean>) {
    map.forEach { bool ->
        if (bool) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(16.dp)
                ) {
                    val end = if (last) size.height / 2 else size.height

                    drawLine(
                        color = Color.Gray,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, end),
                        strokeWidth = 2f
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
            )
        }
        if (map.size > 1) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
            )
        }
    }
//    if (map.isNotEmpty()){
//        Box(
//            modifier = Modifier
//                .width(16.dp)
//                .fillMaxHeight(),
//            contentAlignment = Alignment.Center
//        ) {
//            Canvas(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(16.dp)
//            ) {
//                val end = if (last) size.height / 2 else size.height
//                drawLine(
//                    color = Color.Gray,
//                    start = Offset(size.width, 0f),
//                    end = Offset(size.width, end),
//                    strokeWidth = 2f
//                )
//            }
//        }
//    }
}

@Composable
fun HorizontalLine() {
    Box(
        modifier = Modifier
            .width(16.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color.Gray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
fun SubTaskCard(
    task: Task,
    homeScreenViewModel: HomeScreenViewModel,
    depth: Int = 0,
    last: Boolean = false,
    list: List<Boolean> = listOf(),
) {
    Column {
        var expanded by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(
            targetValue = if (expanded) 90f else 0f,
            label = "iconRotation"
        )
        val parentLast =
            task.parents.lastOrNull() == task.parents.lastOrNull()?.parents?.lastOrNull()?.children?.lastOrNull() // is parent last child of grandparent
        val passingList = list.toMutableList()

        var showDeleteDialog by remember { mutableStateOf(false) }
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
//            IndentationLines(depth, last = last, parentLast = parentLast)
            Alpha(last, passingList)
            passingList.add(parentLast)
            if (depth != 0) HorizontalLine()
            TaskCardNew(
                task = task,
                onClick = {
                    if (task.children.isNotEmpty()) expanded =
                        !expanded else homeScreenViewModel.toggleDoneStatus(task)
                },
                contextMenuOptions = contextMenuOptions(
                    task,
                    homeScreenViewModel
                ) { showDeleteDialog = true },
                icons =
                    if (task.children.isNotEmpty()) {
                        listOf(
                            TaskIconAction(
                                icon = Icons.Default.ChevronRight,
                                description = "Expand Subtask",
                                onClick = { expanded = !expanded },
                                modifier = Modifier.rotate(rotation)
                            )
                        )
                    } else {
                        emptyList()
                    },
            )
        }
        if (showDeleteDialog) {
            TaskDeleteDialog(
                task = task,
                onDismiss = { showDeleteDialog = !showDeleteDialog },
                onDeleteClick = homeScreenViewModel::deleteTask
            )
        }
        if (task.children.isNotEmpty() && expanded) {
            task.children.forEach { child ->
                key(child.id) {
                    SubTaskCard(
                        task = child,
                        homeScreenViewModel = homeScreenViewModel,
                        depth = depth + 1,
                        last = child == task.children.last(),
                        list = passingList
                    )
                }
            }
        }
    }
}