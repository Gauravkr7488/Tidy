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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.subTaskComponents.SubTaskCard
import com.example.tidy.ui.component.topAppBar.TopAppBar
import com.example.tidy.viewModels.HomeScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(isOnTop) {
        if (isOnTop) {
            homeScreenViewModel.refreshTasks()
        }
    }
    val hasDoneTask = tasks.any { it.done }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                AnimatedVisibility(
                    visible = hasDoneTask,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { homeScreenViewModel.cleanCompletedTasks() },
                        modifier = Modifier.padding(
                            bottom = 16.dp,
                            start = 5.dp,
                            end = 5.dp
                        ) // padding for shadows
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Completed"
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { navController.navigate("${Routes.ADD_TASK}/${0}") },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Create new Task"
                    )
                }

            }
        },
        topBar = {
            var doneTaskCount = 0
            tasks.forEach { task ->
                if (task.done) doneTaskCount++
            }
            TopAppBar("My Tasks", subtitle = "$doneTaskCount/${tasks.size} tasks Completed")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = 5.dp, end = 5.dp)
        ) {
            if (tasks.isEmpty()) {
                EmptyTaskList()
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 150.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        tasks.filter { !it.done },
                        key = { "undone-${it.id}" }) { task -> // undone cause the unique key is needed for click
                        SubTaskCard(
                            task, homeScreenViewModel, modifier = Modifier.animateItem(),
                        )
                    }

                    item { Spacer(modifier = Modifier.heightIn(10.dp)) }

                    items(tasks.filter { it.done }, key = { it.id }) { task ->
                        SubTaskCard(
                            task, homeScreenViewModel, modifier = Modifier.animateItem(),
                        )
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