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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tidy.DbOperation
import com.example.tidy.ExportManager
import com.example.tidy.constants.Routes
import com.example.tidy.ui.component.BottomBar
import com.example.tidy.viewModels.AddTaskScreenViewModel
import com.example.tidy.viewModels.BackupScreenViewModel
import com.example.tidy.viewModels.HomeScreenViewModel
import com.example.tidy.viewModels.NoteScreenViewModel
import com.example.tidy.viewModels.SettingsScreenViewModel

@Composable
fun MainScreen(dbOperation: DbOperation, exportManager: ExportManager) {
    val navController = rememberNavController()
    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val addTaskScreenViewModel =
        remember { AddTaskScreenViewModel(dbOperation, navController = navController) }
    val homeScreenViewModel =
        remember { HomeScreenViewModel(dbOperation, exportManager, navController = navController) }
    val noteScreenViewModel = remember { NoteScreenViewModel(dbOperation) }
    val backupScreenViewModel = remember { BackupScreenViewModel(dbOperation) }
    val settingsScreenViewModel = remember { SettingsScreenViewModel(dbOperation) }
    val tabs = listOf(Routes.HOME, Routes.MENU, Routes.SETTINGS)
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val currentPage = tabs[pagerState.currentPage]
    Scaffold(
        bottomBar = {
            if (currentRoute == Routes.HOME) {
                BottomBar(currentPage, pagerState)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.HOME) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 2, // keeps all 3 pages alive
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomeScreen(homeScreenViewModel, navController)
                        1 -> MenuScreen(navController)
                        2 -> SettingsScreen(settingsScreenViewModel, navController)
                    }
                }
            }

            composable(Routes.NOTE) {
                NoteScreen(noteScreenViewModel, navController)
            }

            composable("${Routes.ADD_TASK}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLong()
                if (taskId == null) {
                    AddTaskScreen(
                        addTaskScreenViewModel,
                        navController,
                    )
                } else {
                    AddTaskScreen(
                        addTaskScreenViewModel,
                        navController,
                        taskId = taskId,
                    )
                }
            }

            composable(Routes.ADD_TASK) {
                AddTaskScreen(
                    addTaskScreenViewModel,
                    navController,
                )
            }

            composable(Routes.BACKUP) {
                BackupScreen(backupScreenViewModel, navController)
            }

            composable(Routes.SEARCH) {
                SearchScreen(homeScreenViewModel, navController)
            }
        }
    }
}