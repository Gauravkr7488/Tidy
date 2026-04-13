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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.taskComponents.TaskCardNew
import com.example.tidy.ui.component.taskComponents.TaskIconAction
import com.example.tidy.viewModels.NoteScreenViewModel

@Composable
fun NoteScreen(
    noteScreenViewModel: NoteScreenViewModel,
    navController: NavController,
    modifier: Modifier = Modifier

) {
    val tasks = noteScreenViewModel.tasks.filter { task -> task.note }
    val listState = rememberLazyListState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isOnTop = currentBackStackEntry?.destination?.route == Routes.NOTE

    LaunchedEffect(isOnTop) {
        if (isOnTop) {
            noteScreenViewModel.refreshTasks()
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),

        ) { innerPadding ->


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "My Notes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = 150.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskCardNew(
                        task = task,
                        onClick = { navController.navigate("${Routes.ADD_TASK}/${task.id}") },
                        icons = listOf(
                            TaskIconAction(
                                icon = Icons.AutoMirrored.Filled.Article,
                                description = "Note",
                                onClick = {},
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        )
                    )
                }
            }
        }
    }
}