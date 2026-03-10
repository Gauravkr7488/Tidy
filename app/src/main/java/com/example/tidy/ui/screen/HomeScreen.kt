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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.*
import com.example.tidy.ui.component.TaskItem
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.tidy.viewModels.TaskViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    navController: NavController,

    modifier: Modifier = Modifier
) {
    val tasks = viewModel.tasks
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
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
    // This will run whenever HomeScreen is visible again
    LaunchedEffect(currentBackStackEntry) {
        viewModel.refreshTasks()
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
                            viewModel.cleanCompletedTasks()
                            viewModel.refreshTasks()
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
                contentPadding = PaddingValues(bottom = 150.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        viewModel
                    )
                }
            }
        }
    }
}