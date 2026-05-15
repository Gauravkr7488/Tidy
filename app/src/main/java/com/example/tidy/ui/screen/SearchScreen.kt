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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.taskComponents.TaskCard
import com.example.tidy.ui.component.taskComponents.TaskIconAction
import com.example.tidy.ui.component.topAppBar.TopAppBar
import com.example.tidy.viewModels.HomeScreenViewModel

enum class SearchFilter { ALL, TASKS }

@Composable
fun SearchScreen(
    homeScreenViewModel: HomeScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val tasks = homeScreenViewModel.tasks
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    val filteredTasks = tasks.filter { task ->
        val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true)

        matchesQuery
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isOnTop = currentBackStackEntry?.destination?.route == Routes.SEARCH

    LaunchedEffect(isOnTop) {
        if (isOnTop) {
            homeScreenViewModel.refreshTasks()
        }
    }

    Scaffold(topBar = { TopAppBar("Search") }, modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Search Field
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search tasks and notes...") },
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
                                    SearchFilter.TASKS -> "Tasks"
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
                            children = emptyList(),
                            trailingIcons = buildList {
                                if (task.hide == 1L) {
                                    add(
                                        TaskIconAction(
                                            icon = Icons.Default.Archive,
                                            description = "Archived",
                                            onClick = {},
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
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