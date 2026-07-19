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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tidy.Utils
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskContextAction
import com.example.tidy.ui.component.taskComponents.TaskDeleteDialog
import com.example.tidy.ui.component.topAppBar.TopAppBar
import com.example.tidy.viewModels.SharedViewModel
import com.tidy.sqldelight.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SearchFilter { ALL, REPEAT, ARCHIVED, PARENTS }

@Composable
fun SearchScreen(
    sharedViewModel: SharedViewModel,
    navController: NavController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val taskState = sharedViewModel.tasks.collectAsState()
    val tasks = taskState.value
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    val coroutineScope = rememberCoroutineScope()
    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var deleteTask: Task = Utils.getEmptyTask()
    val filteredTasks = tasks.filter { task ->
        val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            SearchFilter.REPEAT -> task.repeatType != RepeatTypes.NONE
            SearchFilter.PARENTS -> tasks.find { it.parentId == task.id } != null
            SearchFilter.ARCHIVED -> task.hide == 1L
            else -> true
        }

        matchesQuery && matchesFilter
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(pagerState.settledPage) {
        keyboardController?.hide()
        delay(100)
        focusManager.clearFocus()
    }
    Scaffold(topBar = { TopAppBar("Search") }, modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Search Field
            OutlinedTextField( // todo replace with composable in addTask
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search tasks") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(visible = query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SearchFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    SearchFilter.ALL -> "All"
                                    SearchFilter.REPEAT -> "Repeat"
                                    SearchFilter.PARENTS -> "Parents"
                                    SearchFilter.ARCHIVED -> "Archived"
                                }
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results
            if (filteredTasks.isEmpty()) {
                EmptySearchState(query = query)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(
                        start = 5.dp,
                        end = 5.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp
                    ),
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onClick = { navController.navigate("${Routes.ADD_TASK}/${task.id}") },
                            children = sharedViewModel.tasks.collectAsState().value.filter { it.parentId == task.id },
                            contextMenuOptions =
                                buildList {
                                    if (task.hide == 0L) {
                                        add(
                                            TaskContextAction(
                                                label = "Archive",
                                                icon = Icons.Default.Archive,
                                                description = "Archive Task",
                                                onClick = {
                                                    coroutineScope.launch {
                                                        sharedViewModel.saveTask(task.copy(hide = 1L))
                                                    }
                                                },
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    } else {
                                        add(
                                            TaskContextAction(
                                                label = "Unarchive",
                                                icon = Icons.Default.Unarchive,
                                                description = "Unarchive Task",
                                                onClick = {
                                                    coroutineScope.launch {
                                                        sharedViewModel.saveTask(task.copy(hide = 0L))
                                                    }
                                                },
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        )
                                    }
                                    add(
                                        TaskContextAction(
                                            label = "Delete",
                                            icon = Icons.Default.Delete,
                                            description = "Delete Task",
                                            onClick = {
                                                deleteTask = task
                                                showDeleteTaskDialog = true
                                            },
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                        )
                    }
                }
            }
            if (showDeleteTaskDialog) {
                TaskDeleteDialog(
                    task = deleteTask,
                    children = tasks.filter { it.parentId == deleteTask.id },
                    onDismiss = { showDeleteTaskDialog = false }
                ) { sharedViewModel.deleteTask(deleteTask.id, it) }
            }
        }
    }
}

@Composable
fun EmptySearchState(
    query: String,
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
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isBlank()) "Search for notes or tasks"
            else "No results for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}